package top.foxball.nekobackend.security

import com.alibaba.fastjson2.JSON
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import top.foxball.nekobackend.shared.ResponseBuilder
import java.nio.charset.StandardCharsets

@Component
class RestAuthenticationEntryPoint(
    private val responseBuilder: ResponseBuilder,
) : AuthenticationEntryPoint {

    /** 未登录或 token 无效时返回 401 JSON。 */
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val entity = responseBuilder.unauthorized().build()
        response.status = entity.statusCode.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()
        response.writer.write(JSON.toJSONString(entity.body))
        response.writer.flush()
    }
}