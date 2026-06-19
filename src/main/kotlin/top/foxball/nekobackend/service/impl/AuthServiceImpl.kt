package top.foxball.nekobackend.service.impl

import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
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
    private val inviteCodeService: InviteCodeService,
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

    @Transactional
    override fun register(request: RegisterRequest, userAgent: String): RegisterResponse {
        val username = request.username.trim()
        val email = request.email.trim()

        if (
            username.isBlank() ||
            request.password.isBlank() ||
            email.isBlank() ||
            request.verificationCode.isBlank() ||
            request.inviteCode.isBlank()
        ) {
            throw ParamErrorException("用户名、密码、邮箱、验证码和邀请码不能为空")
        }

        emailVerificationService.verifyCode(
            username = username,
            email = email,
            code = request.verificationCode,
            purpose = EmailVerificationPurpose.REGISTER,
            userAgent = userAgent,
            consumeOnSuccess = false,
        )
        val inviteCode = inviteCodeService.validateForRegister(request.inviteCode, email, userAgent)

        val user = userService.createUser(
            username = username,
            password = request.password,
            email = email,
        )
        inviteCodeService.consumeForRegister(
            inviteCode = inviteCode,
            user = user,
            email = email,
            userAgent = userAgent,
        )
        deleteRegisterEmailCodeAfterCommit(username, email)

        return user.toRegisterResponse()
    }

    override fun sendChangePasswordEmailCode(
        userId: Long,
        userAgent: String,
    ): SendEmailVerificationCodeResponse {
        val user = userService.findById(userId) ?: throw UserNotFoundException()
        if (user.status != Status.ACTIVE) {
            throw UserDisabledException()
        }

        return emailVerificationService.sendCode(
            username = user.username,
            email = user.email,
            purpose = EmailVerificationPurpose.CHANGE_PASSWORD,
            userAgent = userAgent,
        )
    }

    @Transactional
    override fun changePassword(
        userId: Long,
        request: ChangePasswordRequest,
        userAgent: String,
    ): ChangePasswordResponse {
        if (
            request.oldPassword.isBlank() ||
            request.newPassword.isBlank() ||
            request.verificationCode.isBlank()
        ) {
            throw ParamErrorException("旧密码、新密码和验证码不能为空")
        }

        val user = userService.findById(userId) ?: throw UserNotFoundException()
        if (user.status != Status.ACTIVE) {
            throw UserDisabledException()
        }

        if (!passwordEncoder.matches(request.oldPassword, user.password)) {
            throw UsernameOrPasswordErrorException("旧密码错误")
        }

        emailVerificationService.verifyCode(
            username = user.username,
            email = user.email,
            code = request.verificationCode,
            purpose = EmailVerificationPurpose.CHANGE_PASSWORD,
            userAgent = userAgent,
            consumeOnSuccess = false,
        )

        user.password = passwordEncoder.encode(request.newPassword)
            ?: throw IllegalStateException("Password encoding failed.")
        userService.save(user)
        jwtSessionService.revokeAll(userId)
        deleteChangePasswordEmailCodeAfterCommit(user.username, user.email)

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
                identity = user.identity,
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
                tags = user.tags.toTagResponses(),
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
            identity = identity,
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

    private fun deleteRegisterEmailCodeAfterCommit(username: String, email: String) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            emailVerificationService.deleteCode(
                username = username,
                email = email,
                purpose = EmailVerificationPurpose.REGISTER,
            )
            return
        }

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    emailVerificationService.deleteCode(
                        username = username,
                        email = email,
                        purpose = EmailVerificationPurpose.REGISTER,
                    )
                }
            }
        )
    }

    private fun deleteChangePasswordEmailCodeAfterCommit(username: String, email: String) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            emailVerificationService.deleteCode(
                username = username,
                email = email,
                purpose = EmailVerificationPurpose.CHANGE_PASSWORD,
            )
            return
        }

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    emailVerificationService.deleteCode(
                        username = username,
                        email = email,
                        purpose = EmailVerificationPurpose.CHANGE_PASSWORD,
                    )
                }
            }
        )
    }

    private fun top.foxball.nekobackend.datasource.jdbc.User.toCurrentUserResponse(): CurrentUserResponse {
        return CurrentUserResponse(
            id = id ?: throw IllegalStateException("User id is missing."),
            username = username,
            email = email,
            nickname = nickname,
            identity = identity,
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
            tags = tags.toTagResponses(),
        )
    }
}
