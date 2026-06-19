package top.foxball.nekobackend.service

import org.springframework.stereotype.Service

enum class EmailVerificationPurpose(
    val redisValue: String,
    val mailTitle: String,
) {
    REGISTER("register", "注册验证码"),
    CHANGE_EMAIL("change_email", "修改邮箱验证码"),
    CHANGE_PASSWORD("change_password", "修改密码验证码"),
}

data class SendEmailVerificationCodeResponse(
    val sent: Boolean,
    val expiresInSeconds: Long,
)

@Service
interface EmailVerificationService {
    fun sendCode(
        username: String,
        email: String,
        purpose: EmailVerificationPurpose,
        userAgent: String,
    ): SendEmailVerificationCodeResponse

    fun verifyCode(
        username: String,
        email: String,
        code: String,
        purpose: EmailVerificationPurpose,
        userAgent: String,
        consumeOnSuccess: Boolean = true,
    )

    fun deleteCode(
        username: String,
        email: String,
        purpose: EmailVerificationPurpose,
    )
}
