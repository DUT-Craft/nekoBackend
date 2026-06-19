package top.foxball.nekobackend.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import top.foxball.nekobackend.security.AuthPrincipal
import top.foxball.nekobackend.service.*
import top.foxball.nekobackend.shared.Response
import top.foxball.nekobackend.shared.ResponseBuilder

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val builder: ResponseBuilder,
) {

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
                authorities = login.user.authorities,
            ),
        )

        return builder.ok().data(rs).build()
    }

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

    @PutMapping("/password")
    fun changePassword(
        authentication: Authentication,
        @RequestBody request: ChangePasswordRequest,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val changePassword = authService.changePassword(principal.userId, request)

        data class Response(
            val changed: Boolean,
        )

        val rs = Response(
            changed = changePassword.changed,
        )

        return builder.ok().data(rs).build()
    }

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
            val authorities: List<String>,
        )

        val rs = Response(
            id = user.id,
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
            contactInformation = user.contactInformation,
            authorities = authorities,
        )

        return builder.ok().data(rs).build()
    }
}
