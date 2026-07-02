package top.foxball.nekobackend.service.impl

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.nekobackend.datasource.jdbc.*
import top.foxball.nekobackend.handlder.ParamErrorException
import top.foxball.nekobackend.handlder.ResourceNotFoundException
import top.foxball.nekobackend.service.CreateInviteCodeRequest
import top.foxball.nekobackend.service.CreateInviteCodeResult
import top.foxball.nekobackend.service.InviteCodeService
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.LocalDateTime

@Service
class InviteCodeServiceImpl(
    private val inviteCodeRepository: InviteCodeRepository,
    private val inviteUseRecordRepository: InviteUseRecordRepository,
    private val stringRedisTemplate: StringRedisTemplate,
) : InviteCodeService {

    private val secureRandom = SecureRandom()

    @Transactional
    override fun create(
        createdByUserId: Long,
        request: CreateInviteCodeRequest,
    ): CreateInviteCodeResult {
        return createInternal(
            createdByUserId = createdByUserId,
            request = request,
            forceSingleUse = false,
        )
    }

    @Transactional
    override fun createForUser(
        createdByUserId: Long,
        request: CreateInviteCodeRequest,
    ): CreateInviteCodeResult {
        if (inviteCodeRepository.countByCreatedByUserId(createdByUserId) >= MAX_USER_INVITE_CODES) {
            throw ParamErrorException("个人邀请码数量已达上限")
        }

        return createInternal(
            createdByUserId = createdByUserId,
            request = request.copy(maxUses = 1),
            forceSingleUse = true,
        )
    }

    private fun createInternal(
        createdByUserId: Long,
        request: CreateInviteCodeRequest,
        forceSingleUse: Boolean,
    ): CreateInviteCodeResult {
        if (request.maxUses < 1) {
            throw ParamErrorException("最大使用次数必须大于 0")
        }

        val plainCode = request.code?.trim()?.takeIf { it.isNotBlank() } ?: generateInviteCode()
        if (plainCode.length < MIN_INVITE_CODE_LENGTH) {
            throw ParamErrorException("邀请码长度不能小于 5 位")
        }
        val codeHash = hashInviteCode(plainCode)
        if (inviteCodeRepository.findByCodeHash(codeHash) != null) {
            throw ParamErrorException("邀请码已存在")
        }

        val now = LocalDateTime.now()
        val expiresAt = request.expiresAt
        if (expiresAt != null && !expiresAt.isAfter(now)) {
            throw ParamErrorException("过期时间必须晚于当前时间")
        }

        val bindEmail = normalizeNullable(request.bindEmail)
            ?: throw ParamErrorException("邀请码必须绑定邮箱")
        emailDomain(bindEmail)

        val saved = inviteCodeRepository.save(
            InviteCode(
                codeHash = codeHash,
                createdByUserId = createdByUserId,
                maxUses = if (forceSingleUse) 1 else request.maxUses,
                usedCount = 0,
                expiresAt = expiresAt,
                status = InviteCodeStatus.ACTIVE,
                bindEmail = bindEmail,
                bindEmailDomain = null,
                remark = normalizeNullable(request.remark),
            )
        )

        return CreateInviteCodeResult(
            inviteCode = saved,
            plainCode = plainCode,
        )
    }

    override fun list(
        page: Int,
        size: Int,
        status: InviteCodeStatus?,
    ): Page<InviteCode> {
        val pageable = pageRequest(page, size)
        return if (status == null) {
            inviteCodeRepository.findAll(pageable)
        } else {
            inviteCodeRepository.findByStatus(status, pageable)
        }
    }

    override fun findById(id: Long): InviteCode {
        return inviteCodeRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("邀请码不存在") }
    }

    @Transactional
    override fun disable(id: Long): InviteCode {
        val inviteCode = findById(id)
        inviteCode.status = InviteCodeStatus.DISABLED
        return inviteCodeRepository.save(inviteCode)
    }

    override fun listRecords(
        inviteCodeId: Long,
        page: Int,
        size: Int,
    ): Page<InviteUseRecord> {
        if (!inviteCodeRepository.existsById(inviteCodeId)) {
            throw ResourceNotFoundException("邀请码不存在")
        }
        return inviteUseRecordRepository.findByInviteCodeIdOrderByUsedAtDesc(
            inviteCodeId = inviteCodeId,
            pageable = recordPageRequest(page, size),
        )
    }

    override fun validateForRegister(inviteCode: String, email: String, userAgent: String): InviteCode {
        val normalizedCode = inviteCode.trim()
        val normalizedEmail = email.trim()
        val normalizedUserAgent = userAgent.trim()
        if (normalizedCode.isBlank()) {
            throw ParamErrorException("邀请码不能为空")
        }
        if (normalizedEmail.isBlank()) {
            throw ParamErrorException("邮箱不能为空")
        }

        ensureInviteFailureAllowed(normalizedEmail, normalizedUserAgent)

        val code = inviteCodeRepository.findByCodeHashForUpdate(hashInviteCode(normalizedCode))
        if (code == null) {
            recordInviteFailure(normalizedEmail, normalizedUserAgent)
            throw ParamErrorException("邀请码无效")
        }

        try {
            ensureUsable(code, normalizedEmail)
            clearInviteFailures(normalizedEmail, normalizedUserAgent)
            return code
        } catch (ex: ParamErrorException) {
            recordInviteFailure(normalizedEmail, normalizedUserAgent)
            throw ex
        }
    }

    override fun consumeForRegister(
        inviteCode: InviteCode,
        user: User,
        email: String,
        userAgent: String,
    ) {
        ensureUsable(inviteCode, email.trim())

        val userId = user.id ?: throw IllegalStateException("Registered user id is missing.")
        val inviteCodeId = inviteCode.id ?: throw IllegalStateException("Invite code id is missing.")

        inviteCode.usedCount += 1
        inviteCodeRepository.save(inviteCode)

        inviteUseRecordRepository.save(
            InviteUseRecord(
                inviteCodeId = inviteCodeId,
                inviteCodeHash = inviteCode.codeHash,
                registeredUserId = userId,
                username = user.username,
                email = email.trim(),
                userAgent = userAgent.trim(),
            )
        )
    }

    private fun ensureUsable(inviteCode: InviteCode, email: String) {
        if (inviteCode.status == InviteCodeStatus.DISABLED) {
            throw ParamErrorException("邀请码不可用")
        }
        if (inviteCode.status == InviteCodeStatus.EXPIRED) {
            throw ParamErrorException("邀请码已过期")
        }

        val expiresAt = inviteCode.expiresAt
        if (expiresAt != null && !expiresAt.isAfter(LocalDateTime.now())) {
            inviteCode.status = InviteCodeStatus.EXPIRED
            inviteCodeRepository.save(inviteCode)
            throw ParamErrorException("邀请码已过期")
        }

        if (inviteCode.usedCount >= inviteCode.maxUses) {
            throw ParamErrorException("邀请码已被使用")
        }

        val bindEmail = inviteCode.bindEmail?.trim()?.takeIf { it.isNotBlank() }
            ?: throw ParamErrorException("邀请码未绑定邮箱")
        if (!bindEmail.equals(email, ignoreCase = true)) {
            throw ParamErrorException("邀请码与邮箱不匹配")
        }
    }

    private fun emailDomain(email: String): String {
        val atIndex = email.lastIndexOf('@')
        if (atIndex < 0 || atIndex == email.lastIndex) {
            throw ParamErrorException("邮箱格式不正确")
        }
        return email.substring(atIndex + 1)
    }

    private fun pageRequest(page: Int, size: Int): PageRequest {
        val normalizedPage = page.coerceAtLeast(0)
        val normalizedSize = size.coerceIn(1, 100)
        return PageRequest.of(normalizedPage, normalizedSize, Sort.by(Sort.Direction.DESC, "createdAt"))
    }

    private fun recordPageRequest(page: Int, size: Int): PageRequest {
        val normalizedPage = page.coerceAtLeast(0)
        val normalizedSize = size.coerceIn(1, 100)
        return PageRequest.of(normalizedPage, normalizedSize, Sort.by(Sort.Direction.DESC, "usedAt"))
    }

    private fun normalizeNullable(value: String?): String? {
        return value?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun generateInviteCode(): String {
        repeat(MAX_GENERATE_ATTEMPTS) {
            val code = "NEKO-" + randomCodeChunk() + "-" + randomCodeChunk() + "-" + randomCodeChunk()
            if (inviteCodeRepository.findByCodeHash(hashInviteCode(code)) == null) {
                return code
            }
        }
        throw IllegalStateException("Failed to generate unique invite code.")
    }

    private fun randomCodeChunk(): String {
        return buildString {
            repeat(INVITE_CODE_CHUNK_LENGTH) {
                append(INVITE_CODE_ALPHABET[secureRandom.nextInt(INVITE_CODE_ALPHABET.length)])
            }
        }
    }

    private fun ensureInviteFailureAllowed(email: String, userAgent: String) {
        val attempts = stringRedisTemplate.opsForValue().get(inviteFailureKey(email, userAgent))?.toIntOrNull() ?: 0
        if (attempts >= MAX_INVITE_FAILURES) {
            throw ParamErrorException("邀请码错误次数过多，请稍后重试")
        }
    }

    private fun recordInviteFailure(email: String, userAgent: String) {
        val key = inviteFailureKey(email, userAgent)
        val attempts = stringRedisTemplate.opsForValue().increment(key) ?: return
        if (attempts == 1L) {
            stringRedisTemplate.expire(key, INVITE_FAILURE_TTL)
        }
    }

    private fun clearInviteFailures(email: String, userAgent: String) {
        stringRedisTemplate.delete(inviteFailureKey(email, userAgent))
    }

    private fun inviteFailureKey(email: String, userAgent: String): String {
        return "auth:invite_code:fail:${sha256(email.trim().lowercase())}:${sha256(userAgent.trim())}"
    }

    private fun hashInviteCode(code: String): String {
        return sha256(code.trim().uppercase())
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val MIN_INVITE_CODE_LENGTH = 5
        private const val INVITE_CODE_CHUNK_LENGTH = 4
        private const val MAX_GENERATE_ATTEMPTS = 10
        private const val MAX_USER_INVITE_CODES = 5L
        private const val MAX_INVITE_FAILURES = 5
        private const val INVITE_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        private val INVITE_FAILURE_TTL: Duration = Duration.ofMinutes(10)
    }
}
