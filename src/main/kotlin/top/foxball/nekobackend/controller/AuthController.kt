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
                authorities = login.user.authorities,
            ),
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<Response> {
        val register = authService.register(request)

        data class Response(
            val id: Long,
            val username: String,
            val email: String,
            val nickname: String,
            val avatar: String?,
        )

        val rs = Response(
            id = register.id,
            username = register.username,
            email = register.email,
            nickname = register.nickname,
            avatar = register.avatar,
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

        data class Response(
            val id: Long,
            val username: String,
            val authorities: List<String>,
        )

        val rs = Response(
            id = principal.userId,
            username = principal.username,
            authorities = authorities,
        )

        return builder.ok().data(rs).build()
    }
}
