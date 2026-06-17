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
    val authorities: List<String>,
)

data class RegisterRequest(
    val username: String = "",
    val password: String = "",
    val email: String = "",
)

data class RegisterResponse(
    val id: Long,
    val username: String,
    val email: String,
    val nickname: String,
    val avatar: String?,
)

data class ChangePasswordRequest(
    val oldPassword: String = "",
    val newPassword: String = "",
)

data class ChangePasswordResponse(
    val changed: Boolean,
)

@Service
interface AuthService {
    fun register(request: RegisterRequest): RegisterResponse
    fun changePassword(userId: Long, request: ChangePasswordRequest): ChangePasswordResponse
    fun login(request: LoginRequest, userAgent: String): LoginResponse
}
