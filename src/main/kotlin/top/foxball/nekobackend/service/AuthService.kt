package top.foxball.nekobackend.service

import org.springframework.stereotype.Service

data class LoginRequest(
    val username: String = "",
    val password: String = "",
)

data class LoginResponse(
    val tokenType: String,
    val accessToken: String,
    val expiresIn: Long,
    val user: LoginUserResponse,
)

data class LoginUserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val nickname: String,
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

data class RegisterRequest(
    val username: String = "",
    val password: String = "",
    val email: String = "",
    val verificationCode: String = "",
    val inviteCode: String = "",
)

data class RegisterResponse(
    val id: Long,
    val username: String,
    val email: String,
    val nickname: String,
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

data class CurrentUserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val nickname: String,
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

data class ChangePasswordRequest(
    val oldPassword: String = "",
    val newPassword: String = "",
    val verificationCode: String = "",
)

data class ChangePasswordResponse(
    val changed: Boolean,
)

data class SendRegisterEmailCodeRequest(
    val username: String = "",
    val email: String = "",
)

@Service
interface AuthService {
    fun sendRegisterEmailCode(
        request: SendRegisterEmailCodeRequest,
        userAgent: String,
    ): SendEmailVerificationCodeResponse

    fun register(request: RegisterRequest, userAgent: String): RegisterResponse
    fun sendChangePasswordEmailCode(userId: Long, userAgent: String): SendEmailVerificationCodeResponse
    fun changePassword(userId: Long, request: ChangePasswordRequest, userAgent: String): ChangePasswordResponse
    fun login(request: LoginRequest, userAgent: String): LoginResponse
    fun currentUser(userId: Long): CurrentUserResponse
}
