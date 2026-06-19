package top.foxball.nekobackend.service.impl

import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import top.foxball.nekobackend.datasource.jdbc.Status
import top.foxball.nekobackend.handlder.ParamErrorException
import top.foxball.nekobackend.handlder.UserAlreadyExistsException
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
    private val emailVerificationService: EmailVerificationService,
) : AuthService {

    override fun sendRegisterEmailCode(
        request: SendRegisterEmailCodeRequest,
        userAgent: String,
    ): SendEmailVerificationCodeResponse {
        val username = request.username.trim()
        val email = request.email.trim()

        if (username.isBlank() || email.isBlank()) {
            throw ParamErrorException("用户名和邮箱不能为空")
        }
        if (userService.findByUsername(username) != null) {
            throw UserAlreadyExistsException("用户名已存在")
        }
        if (userService.findByEmail(email) != null) {
            throw UserAlreadyExistsException("邮箱已存在")
        }

        return emailVerificationService.sendCode(
            username = username,
            email = email,
            purpose = EmailVerificationPurpose.REGISTER,
            userAgent = userAgent,
        )
    }

    override fun register(request: RegisterRequest, userAgent: String): RegisterResponse {
        val username = request.username.trim()
        val email = request.email.trim()

        if (username.isBlank() || request.password.isBlank() || email.isBlank() || request.verificationCode.isBlank()) {
            throw ParamErrorException("用户名、密码、邮箱和验证码不能为空")
        }

        emailVerificationService.verifyCode(
            username = username,
            email = email,
            code = request.verificationCode,
            purpose = EmailVerificationPurpose.REGISTER,
            userAgent = userAgent,
        )

        val user = userService.createUser(
            username = username,
            password = request.password,
            email = email,
        )

        return user.toRegisterResponse()
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
        if (userAgent.isBlank()) {
            throw ParamErrorException("User-Agent 不能为空")
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
        val user = userService.findById(principal.userId) ?: throw UserNotFoundException()
        val accessToken = jwtTokenService.createAccessToken(
            userId = principal.userId,
            username = principal.username,
            authorities = authorities,
        )
        jwtSessionService.save(
            token = accessToken,
            userId = principal.userId,
            userAgent = userAgent.trim(),
        )

        return LoginResponse(
            tokenType = "Bearer",
            accessToken = accessToken,
            expiresIn = jwtProperties.accessTokenTtlSeconds,
            user = LoginUserResponse(
                id = user.id ?: principal.userId,
                username = user.username,
                email = user.email,
                nickname = user.nickname,
                avatar = user.avatar,
                signature = user.signature,
                studentId = user.studentId,
                grade = user.grade,
                className = user.className,
                major = user.major,
                phone = user.phone,
                qqNumber = user.qqNumber,
                isStudentId = user.isStudentId,
                isGrouping = user.isGrouping,
                isClassName = user.isClassName,
                isMajor = user.isMajor,
                isPhone = user.isPhone,
                isQQNumber = user.isQQNumber,
                contactInformation = user.contactInformation ?: emptyList(),
                authorities = authorities,
            ),
        )
    }

    override fun currentUser(userId: Long): CurrentUserResponse {
        return userService.findById(userId)?.toCurrentUserResponse() ?: throw UserNotFoundException()
    }

    private fun top.foxball.nekobackend.datasource.jdbc.User.toRegisterResponse(): RegisterResponse {
        return RegisterResponse(
            id = id ?: throw IllegalStateException("Created user id is missing."),
            username = username,
            email = email,
            nickname = nickname,
            avatar = avatar,
            signature = signature,
            studentId = studentId,
            grade = grade,
            className = className,
            major = major,
            phone = phone,
            qqNumber = qqNumber,
            isStudentId = isStudentId,
            isGrouping = isGrouping,
            isClassName = isClassName,
            isMajor = isMajor,
            isPhone = isPhone,
            isQQNumber = isQQNumber,
            contactInformation = contactInformation ?: emptyList(),
        )
    }

    private fun top.foxball.nekobackend.datasource.jdbc.User.toCurrentUserResponse(): CurrentUserResponse {
        return CurrentUserResponse(
            id = id ?: throw IllegalStateException("User id is missing."),
            username = username,
            email = email,
            nickname = nickname,
            avatar = avatar,
            signature = signature,
            studentId = studentId,
            grade = grade,
            className = className,
            major = major,
            phone = phone,
            qqNumber = qqNumber,
            isStudentId = isStudentId,
            isGrouping = isGrouping,
            isClassName = isClassName,
            isMajor = isMajor,
            isPhone = isPhone,
            isQQNumber = isQQNumber,
            contactInformation = contactInformation ?: emptyList(),
        )
    }
}
