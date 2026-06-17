package top.foxball.nekobackend.security.jwt

import com.alibaba.fastjson2.JSON
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import top.foxball.nekobackend.handlder.BusinessException
import top.foxball.nekobackend.handlder.TokenInvalidException
import top.foxball.nekobackend.handlder.UserDisabledException
import top.foxball.nekobackend.security.AuthPrincipal
import top.foxball.nekobackend.shared.Response
import java.nio.charset.StandardCharsets

@Component
class JwtAuthenticationFilter(
    private val jwtTokenService: JwtTokenService,
    private val jwtSessionService: JwtSessionService,
    private val userDetailsService: UserDetailsService,
) : OncePerRequestFilter() {

    /**
     * 过滤器入口。
     *
     * 处理流程：
     * 1. 读取 Bearer token。
     * 2. 验签并解析 JWT。
     * 3. 校验 Redis 登录态和 User-Agent。
     * 4. 构造 Authentication 并写入 SecurityContext。
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (authorization.isNullOrBlank() || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val token = authorization.removePrefix("Bearer ").trim()
            val claims = jwtTokenService.parseAndValidate(token)
            jwtSessionService.validate(
                token = token,
                userId = claims.userId,
                userAgent = request.getHeader(HttpHeaders.USER_AGENT),
            )

            if (SecurityContextHolder.getContext().authentication == null) {
                val userDetails = userDetailsService.loadUserByUsername(claims.username)
                if (userDetails is AuthPrincipal && userDetails.userId != claims.userId) {
                    throw TokenInvalidException()
                }
                if (!userDetails.isEnabled || !userDetails.isAccountNonLocked) {
                    throw UserDisabledException()
                }

                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities,
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }

            filterChain.doFilter(request, response)
        } catch (ex: BusinessException) {
            writeError(response, ex.code, ex.message)
        } catch (ex: UsernameNotFoundException) {
            val tokenInvalid = TokenInvalidException()
            writeError(response, tokenInvalid.code, tokenInvalid.message)
        }
    }

    /** 统一输出 JSON 格式的认证错误。 */
    private fun writeError(
        response: HttpServletResponse,
        status: Int,
        message: String,
    ) {
        response.status = status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()
        response.writer.write(
            JSON.toJSONString(
                Response(
                    status = status,
                    message = message.ifBlank { HttpStatus.valueOf(status).reasonPhrase },
                    data = emptyMap<String, Any>(),
                )
            )
        )
    }
}
