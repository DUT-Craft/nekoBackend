package top.foxball.nekobackend.controller

import org.springframework.http.ResponseEntity
import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.nekobackend.security.AuthPrincipal
import top.foxball.nekobackend.service.AuthService
import top.foxball.nekobackend.service.ChangePasswordRequest
import top.foxball.nekobackend.service.LoginRequest
import top.foxball.nekobackend.service.LoginUserResponse
import top.foxball.nekobackend.service.RegisterRequest
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
        @RequestHeader(HttpHeaders.USER_AGENT, required = false) userAgent: String?,
    ): ResponseEntity<Response> {
        return builder.ok()
            .data(authService.login(request, userAgent))
            .build()
    }

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<Response> {
        return builder.ok()
            .data(authService.register(request))
            .build()
    }

    @PutMapping("/password")
    fun changePassword(
        authentication: Authentication,
        @RequestBody request: ChangePasswordRequest,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal

        return builder.ok()
            .data(authService.changePassword(principal.userId, request))
            .build()
    }

    @GetMapping("/me")
    fun me(authentication: Authentication): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val authorities = principal.authorities.mapNotNull { it.authority }

        return builder.ok()
            .data(
                LoginUserResponse(
                    id = principal.userId,
                    username = principal.username,
                    authorities = authorities,
                )
            )
            .build()
    }
}
