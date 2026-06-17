package top.foxball.nekobackend.security.jwt

import com.alibaba.fastjson2.JSON
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
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
        val userAgent = request.getHeader(HttpHeaders.USER_AGENT)
        if (requiresUserAgent(request) && userAgent.isNullOrBlank()) {
            writeError(response, HttpStatus.BAD_REQUEST.value(), "User-Agent 不能为空")
            return
        }

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
                userAgent = userAgent,
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

    /** 登录接口和非放行接口必须携带 User-Agent；注册、健康检查和预检请求不强制。 */
    private fun requiresUserAgent(request: HttpServletRequest): Boolean {
        if (HttpMethod.OPTIONS.matches(request.method)) return false

        val path = request.servletPath
        if (path == "/api/auth/login") return true

        return path != "/api/auth/register" &&
            path != "/actuator/info" &&
            !path.startsWith("/actuator/health")
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
