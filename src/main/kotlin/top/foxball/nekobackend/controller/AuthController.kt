package top.foxball.nekobackend.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import top.foxball.nekobackend.security.AuthPrincipal
import top.foxball.nekobackend.service.*
import top.foxball.nekobackend.shared.Response
import top.foxball.nekobackend.shared.ResponseBuilder

/**
 * 认证相关接口，负责登录、注册、密码变更和当前登录用户信息查询。
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val builder: ResponseBuilder,
) {

    /**
     * 使用账号密码登录，并返回访问令牌与当前用户基础资料。
     */
    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        @RequestHeader(HttpHeaders.USER_AGENT, required = true) userAgent: String,
    ): ResponseEntity<Response> {
        val login = authService.login(request, userAgent)

        data class UserResponse(
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

        data class Response(
            val tokenType: String,
            val accessToken: String,
            val expiresIn: Long,
            val user: UserResponse,
        )

        val rs = Response(
            tokenType = login.tokenType,
            accessToken = login.accessToken,
            expiresIn = login.expiresIn,
            user = UserResponse(
                id = login.user.id,
                username = login.user.username,
                email = login.user.email,
                nickname = login.user.nickname,
                identity = login.user.identity,
                avatar = login.user.avatar,
                signature = login.user.signature,
                studentId = login.user.studentId,
                grade = login.user.grade,
                className = login.user.className,
                major = login.user.major,
                phone = login.user.phone,
                qqNumber = login.user.qqNumber,
                isStudentId = login.user.isStudentId,
                isGrouping = login.user.isGrouping,
                isClassName = login.user.isClassName,
                isMajor = login.user.isMajor,
                isPhone = login.user.isPhone,
                isQQNumber = login.user.isQQNumber,
                contactInformation = login.user.contactInformation,
                tags = login.user.tags,
                authorities = login.user.authorities,
            ),
        )

        return builder.ok().data(rs).build()
    }

    /**
     * 发送注册邮箱验证码，用于后续注册校验。
     */
    @PostMapping("/email-code/register")
    fun sendRegisterEmailCode(
        @RequestBody request: SendRegisterEmailCodeRequest,
        @RequestHeader(HttpHeaders.USER_AGENT, required = true) userAgent: String,
    ): ResponseEntity<Response> {
        val result = authService.sendRegisterEmailCode(request, userAgent)

        data class Response(
            val sent: Boolean,
            val expiresInSeconds: Long,
        )

        val rs = Response(
            sent = result.sent,
            expiresInSeconds = result.expiresInSeconds,
        )

        return builder.ok().data(rs).build()
    }

    /**
     * 根据注册信息创建用户账号。
     */
    @PostMapping("/register")
    fun register(
        @RequestBody request: RegisterRequest,
        @RequestHeader(HttpHeaders.USER_AGENT, required = true) userAgent: String,
    ): ResponseEntity<Response> {
        val register = authService.register(request, userAgent)

        data class Response(
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

        val rs = Response(
            id = register.id,
            username = register.username,
            email = register.email,
            nickname = register.nickname,
            identity = register.identity,
            avatar = register.avatar,
            signature = register.signature,
            studentId = register.studentId,
            grade = register.grade,
            className = register.className,
            major = register.major,
            phone = register.phone,
            qqNumber = register.qqNumber,
            isStudentId = register.isStudentId,
            isGrouping = register.isGrouping,
            isClassName = register.isClassName,
            isMajor = register.isMajor,
            isPhone = register.isPhone,
            isQQNumber = register.isQQNumber,
            contactInformation = register.contactInformation,
        )

        return builder.ok().data(rs).build()
    }

    /**
     * 向当前登录用户邮箱发送修改密码验证码。
     */
    @PostMapping("/email-code/password")
    fun sendChangePasswordEmailCode(
        authentication: Authentication,
        @RequestHeader(HttpHeaders.USER_AGENT, required = true) userAgent: String,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val result = authService.sendChangePasswordEmailCode(principal.userId, userAgent)

        data class Response(
            val sent: Boolean,
            val expiresInSeconds: Long,
        )

        val rs = Response(
            sent = result.sent,
            expiresInSeconds = result.expiresInSeconds,
        )

        return builder.ok().data(rs).build()
    }

    /**
     * 校验邮箱验证码后修改当前登录用户的密码。
     */
    @PutMapping("/password")
    fun changePassword(
        authentication: Authentication,
        @RequestBody request: ChangePasswordRequest,
        @RequestHeader(HttpHeaders.USER_AGENT, required = true) userAgent: String,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val changePassword = authService.changePassword(principal.userId, request, userAgent)

        data class Response(
            val changed: Boolean,
        )

        val rs = Response(
            changed = changePassword.changed,
        )

        return builder.ok().data(rs).build()
    }

    /**
     * 获取当前登录用户资料和授权信息。
     */
    @GetMapping("/me")
    fun me(authentication: Authentication): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val authorities = principal.authorities.mapNotNull { it.authority }
        val user = authService.currentUser(principal.userId)

        data class Response(
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

        val rs = Response(
            id = user.id,
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
            contactInformation = user.contactInformation,
            tags = user.tags,
            authorities = authorities,
        )

        return builder.ok().data(rs).build()
    }
}
