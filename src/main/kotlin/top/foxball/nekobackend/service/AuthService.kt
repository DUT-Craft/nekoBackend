package top.foxball.nekobackend.service

import org.springframework.stereotype.Service

/**
 * 登录请求，使用用户名和密码换取访问令牌。
 */
data class LoginRequest(
    val username: String = "",
    val password: String = "",
)

/**
 * 登录成功后的令牌信息和当前用户资料。
 */
data class LoginResponse(
    val tokenType: String,
    val accessToken: String,
    val expiresIn: Long,
    val user: LoginUserResponse,
)

/**
 * 登录接口返回的用户资料、标签和权限列表。
 */
data class LoginUserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val nickname: String,
    val identity: String?,
    val avatar: String?,
    val signature: String?,
    val studentId: String?,
    val grade: String?,
    val className: String?,
    val major: String?,
    val phone: String?,
    val qqNumber: String?,
    val isStudentId: Boolean,
    val isGrouping: String?,
    val isClassName: Boolean,
    val isMajor: Boolean,
    val isPhone: Boolean,
    val isQQNumber: Boolean,
    val contactInformation: List<String>,
    val tags: List<TagResponse>,
    val authorities: List<String>,
)

/**
 * 注册请求，包含账号信息、邮箱验证码和邀请码。
 */
data class RegisterRequest(
    val username: String = "",
    val password: String = "",
    val email: String = "",
    val verificationCode: String = "",
    val inviteCode: String = "",
)

/**
 * 注册成功后返回的新用户基础资料。
 */
data class RegisterResponse(
    val id: Long,
    val username: String,
    val email: String,
    val nickname: String,
    val identity: String?,
    val avatar: String?,
    val signature: String?,
    val studentId: String?,
    val grade: String?,
    val className: String?,
    val major: String?,
    val phone: String?,
    val qqNumber: String?,
    val isStudentId: Boolean,
    val isGrouping: String?,
    val isClassName: Boolean,
    val isMajor: Boolean,
    val isPhone: Boolean,
    val isQQNumber: Boolean,
    val contactInformation: List<String>,
)

/**
 * 当前登录用户的完整资料。
 */
data class CurrentUserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val nickname: String,
    val identity: String?,
    val avatar: String?,
    val signature: String?,
    val studentId: String?,
    val grade: String?,
    val className: String?,
    val major: String?,
    val phone: String?,
    val qqNumber: String?,
    val isStudentId: Boolean,
    val isGrouping: String?,
    val isClassName: Boolean,
    val isMajor: Boolean,
    val isPhone: Boolean,
    val isQQNumber: Boolean,
    val contactInformation: List<String>,
    val tags: List<TagResponse>,
)

/**
 * 修改密码请求，需同时提交旧密码、新密码和邮箱验证码。
 */
data class ChangePasswordRequest(
    val oldPassword: String = "",
    val newPassword: String = "",
    val verificationCode: String = "",
)

/**
 * 修改密码操作结果。
 */
data class ChangePasswordResponse(
    val changed: Boolean,
)

/**
 * 注册邮箱验证码发送请求。
 */
data class SendRegisterEmailCodeRequest(
    val username: String = "",
    val email: String = "",
)

/**
 * 认证与账号安全服务，封装注册、登录、改密和当前用户资料查询流程。
 */
@Service
interface AuthService {
    /**
     * 发送注册邮箱验证码。
     *
     * 发送前会校验用户名和邮箱未被占用，并将验证码与 User-Agent 绑定。
     */
    fun sendRegisterEmailCode(
        request: SendRegisterEmailCodeRequest,
        userAgent: String,
    ): SendEmailVerificationCodeResponse

    /**
     * 完成用户注册。
     *
     * 会校验邮箱验证码和邀请码，创建用户后消费邀请码，并在事务提交后清理验证码。
     */
    fun register(request: RegisterRequest, userAgent: String): RegisterResponse

    /**
     * 向指定用户邮箱发送修改密码验证码。
     */
    fun sendChangePasswordEmailCode(userId: Long, userAgent: String): SendEmailVerificationCodeResponse

    /**
     * 校验旧密码和邮箱验证码后修改用户密码，并撤销该用户已有登录会话。
     */
    fun changePassword(userId: Long, request: ChangePasswordRequest, userAgent: String): ChangePasswordResponse

    /**
     * 使用用户名和密码登录，签发访问令牌并保存登录会话。
     */
    fun login(request: LoginRequest, userAgent: String): LoginResponse

    /**
     * 查询当前登录用户资料。
     */
    fun currentUser(userId: Long): CurrentUserResponse
}
