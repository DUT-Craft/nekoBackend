package top.foxball.nekobackend.service

import org.springframework.stereotype.Service

/**
 * 邮箱验证码用途。
 *
 * 每个用途使用独立的 Redis 标识和邮件标题，避免不同业务验证码互相复用。
 */
enum class EmailVerificationPurpose(
    val redisValue: String,
    val mailTitle: String,
) {
    REGISTER("register", "注册验证码"),
    CHANGE_EMAIL("change_email", "修改邮箱验证码"),
    CHANGE_PASSWORD("change_password", "修改密码验证码"),
}

/**
 * 邮箱验证码发送结果。
 */
data class SendEmailVerificationCodeResponse(
    val sent: Boolean,
    val expiresInSeconds: Long,
)

/**
 * 邮箱验证码服务，负责验证码生成、发送、校验和清理。
 */
@Service
interface EmailVerificationService {
    /**
     * 生成验证码并发送到指定邮箱。
     *
     * 验证码会按用户名、邮箱、用途和 User-Agent 绑定保存。
     */
    fun sendCode(
        username: String,
        email: String,
        purpose: EmailVerificationPurpose,
        userAgent: String,
    ): SendEmailVerificationCodeResponse

    /**
     * 校验指定用途的邮箱验证码。
     *
     * 当 [consumeOnSuccess] 为 true 时，验证码校验成功后会立即删除。
     */
    fun verifyCode(
        username: String,
        email: String,
        code: String,
        purpose: EmailVerificationPurpose,
        userAgent: String,
        consumeOnSuccess: Boolean = true,
    )

    /**
     * 删除指定用户、邮箱和用途对应的验证码。
     */
    fun deleteCode(
        username: String,
        email: String,
        purpose: EmailVerificationPurpose,
    )
}
