package top.foxball.nekobackend.service.impl

import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import top.foxball.nekobackend.datasource.jdbc.Status
import top.foxball.nekobackend.handlder.ParamErrorException
import top.foxball.nekobackend.handlder.UserDisabledException
import top.foxball.nekobackend.handlder.UserNotFoundException
import top.foxball.nekobackend.handlder.UsernameOrPasswordErrorException
import top.foxball.nekobackend.security.AuthPrincipal
import top.foxball.nekobackend.security.NekoUserDetailsService
import top.foxball.nekobackend.security.jwt.JwtProperties
import top.foxball.nekobackend.security.jwt.JwtSessionService
import top.foxball.nekobackend.security.jwt.JwtTokenService
import top.foxball.nekobackend.service.*

@Service
class AuthServiceImpl(
    private val userDetailsService: NekoUserDetailsService,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenService: JwtTokenService,
    private val jwtSessionService: JwtSessionService,
    private val jwtProperties: JwtProperties,
) : AuthService {

    override fun register(request: RegisterRequest): RegisterResponse {
        val username = request.username.trim()
        val email = request.email.trim()

        if (username.isBlank() || request.password.isBlank() || email.isBlank()) {
            throw ParamErrorException("用户名、密码和邮箱不能为空")
        }

        val user = userService.createUser(
            username = username,
            password = request.password,
            email = email,
        )

        return RegisterResponse(
            id = user.id ?: throw IllegalStateException("Created user id is missing."),
            username = user.username,
            email = user.email,
            nickname = user.nickname,
            avatar = user.avatar,
        )
    }

    override fun changePassword(userId: Long, request: ChangePasswordRequest): ChangePasswordResponse {
        if (request.oldPassword.isBlank() || request.newPassword.isBlank()) {
            throw ParamErrorException("旧密码和新密码不能为空")
        }

        val user = userService.findById(userId) ?: throw UserNotFoundException()
        if (user.status != Status.ACTIVE) {
            throw UserDisabledException()
        }

        if (!passwordEncoder.matches(request.oldPassword, user.password)) {
            throw UsernameOrPasswordErrorException("旧密码错误")
        }

        user.password = passwordEncoder.encode(request.newPassword)
            ?: throw IllegalStateException("Password encoding failed.")
        userService.save(user)
        jwtSessionService.revokeAll(userId)

        return ChangePasswordResponse(changed = true)
    }

    override fun login(request: LoginRequest, userAgent: String): LoginResponse {
        if (request.username.isBlank() || request.password.isBlank()) {
            throw UsernameOrPasswordErrorException()
        }

        val principal = try {
            userDetailsService.loadUserByUsername(request.username.trim()) as AuthPrincipal
        } catch (ex: UsernameNotFoundException) {
            throw UsernameOrPasswordErrorException()
        }

        if (!passwordEncoder.matches(request.password, principal.password)) {
            throw UsernameOrPasswordErrorException()
        }

        if (!principal.isEnabled) {
            throw UserDisabledException()
        }

        val authorities = principal.authorities.mapNotNull { it.authority }
        val accessToken = jwtTokenService.createAccessToken(
            userId = principal.userId,
            username = principal.username,
            authorities = authorities,
        )
        jwtSessionService.save(
            token = accessToken,
            userId = principal.userId,
            userAgent = userAgent,
        )

        return LoginResponse(
            tokenType = "Bearer",
            accessToken = accessToken,
            expiresIn = jwtProperties.accessTokenTtlSeconds,
            user = LoginUserResponse(
                id = principal.userId,
                username = principal.username,
                authorities = authorities,
            ),
        )
    }
}
