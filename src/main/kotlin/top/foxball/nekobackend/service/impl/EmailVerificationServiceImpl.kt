package top.foxball.nekobackend.service.impl

import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import top.foxball.nekobackend.config.EmailVerificationProperties
import top.foxball.nekobackend.datasource.redis.EmailCode
import top.foxball.nekobackend.datasource.redis.EmailCodeRepository
import top.foxball.nekobackend.handlder.EmailSendFailedException
import top.foxball.nekobackend.handlder.ParamErrorException
import top.foxball.nekobackend.handlder.VerificationCodeInvalidException
import top.foxball.nekobackend.service.EmailVerificationPurpose
import top.foxball.nekobackend.service.EmailVerificationService
import top.foxball.nekobackend.service.SendEmailVerificationCodeResponse
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom

@Service
class EmailVerificationServiceImpl(
    private val mailSender: JavaMailSender,
    private val emailCodeRepository: EmailCodeRepository,
    private val properties: EmailVerificationProperties,
) : EmailVerificationService {

    private val log = LoggerFactory.getLogger(EmailVerificationServiceImpl::class.java)
    private val secureRandom = SecureRandom()

    override fun sendCode(
        username: String,
        email: String,
        purpose: EmailVerificationPurpose,
        userAgent: String,
    ): SendEmailVerificationCodeResponse {
        val normalizedUsername = normalizeUsername(username)
        val normalizedEmail = normalizeEmail(email)
        val normalizedUserAgent = normalizeUserAgent(userAgent)
        val ttlSeconds = properties.ttlSeconds.takeIf { it > 0 } ?: DEFAULT_TTL_SECONDS
        val code = generateCode()
        val key = redisKey(purpose, normalizedUsername, normalizedEmail)
        val record = EmailCode().apply {
            this.id = key
            this.code = code
            this.email = normalizedEmail
            this.userName = normalizedUsername
            this.userAgent = normalizedUserAgent
            this.ttlSeconds = ttlSeconds
        }

        emailCodeRepository.save(record)
        try {
            mailSender.send(buildMessage(normalizedEmail, code, purpose, ttlSeconds))
        } catch (ex: MailException) {
            emailCodeRepository.deleteById(key)
            log.warn(
                "Failed to send email verification code. purpose={}, username={}, email={}",
                purpose.redisValue,
                normalizedUsername,
                maskEmail(normalizedEmail),
                ex,
            )
            throw EmailSendFailedException()
        }

        return SendEmailVerificationCodeResponse(
            sent = true,
            expiresInSeconds = ttlSeconds,
        )
    }

    override fun verifyCode(
        username: String,
        email: String,
        code: String,
        purpose: EmailVerificationPurpose,
        userAgent: String,
    ) {
        val normalizedUsername = normalizeUsername(username)
        val normalizedEmail = normalizeEmail(email)
        val normalizedCode = code.trim()
        val normalizedUserAgent = normalizeUserAgent(userAgent)
        if (normalizedCode.isBlank()) {
            throw VerificationCodeInvalidException()
        }

        val key = redisKey(purpose, normalizedUsername, normalizedEmail)
        val record = emailCodeRepository.findById(key).orElseThrow { VerificationCodeInvalidException() }

        if (
            record.userName != normalizedUsername ||
            record.email != normalizedEmail ||
            record.code != normalizedCode ||
            record.userAgent != normalizedUserAgent
        ) {
            throw VerificationCodeInvalidException()
        }

        emailCodeRepository.deleteById(key)
    }

    private fun buildMessage(
        email: String,
        code: String,
        purpose: EmailVerificationPurpose,
        ttlSeconds: Long,
    ): SimpleMailMessage {
        val message = SimpleMailMessage()
        val from = properties.from.trim()
        if (from.isNotBlank()) {
            message.from = from
        }
        message.setTo(email)
        message.subject = "${properties.subjectPrefix.trim().ifBlank { "NekoBackend" }} ${purpose.mailTitle}"
        message.text = """
            您的${purpose.mailTitle}是：$code

            验证码将在 ${ttlSeconds / 60} 分钟内有效，请勿泄露给他人。
        """.trimIndent()
        return message
    }

    private fun generateCode(): String {
        val length = properties.codeLength.coerceIn(MIN_CODE_LENGTH, MAX_CODE_LENGTH)
        return buildString {
            repeat(length) {
                append(secureRandom.nextInt(10))
            }
        }
    }

    private fun normalizeUsername(username: String): String {
        return username.trim().takeIf { it.isNotBlank() }
            ?: throw ParamErrorException("用户名不能为空")
    }

    private fun normalizeEmail(email: String): String {
        return email.trim().takeIf { it.isNotBlank() }
            ?: throw ParamErrorException("邮箱不能为空")
    }

    private fun normalizeUserAgent(userAgent: String?): String {
        return userAgent?.trim()?.takeIf { it.isNotBlank() }
            ?: throw ParamErrorException("User-Agent 不能为空")
    }

    private fun maskEmail(email: String): String {
        val atIndex = email.indexOf('@')
        if (atIndex <= 1) return "***"

        val prefix = email.take(atIndex)
        val domain = email.drop(atIndex)
        return "${prefix.first()}***${prefix.last()}$domain"
    }

    private fun redisKey(
        purpose: EmailVerificationPurpose,
        username: String,
        email: String,
    ): String {
        return "auth:email_code:${purpose.redisValue}:${sha256(username)}:${sha256(email)}"
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val DEFAULT_TTL_SECONDS = 600L
        private const val MIN_CODE_LENGTH = 4
        private const val MAX_CODE_LENGTH = 10
    }
}
