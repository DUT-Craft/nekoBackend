package top.foxball.nekobackend.security

import com.alibaba.fastjson2.JSON
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import top.foxball.nekobackend.shared.ResponseBuilder
import java.nio.charset.StandardCharsets

@Component
class RestAccessDeniedHandler(
    private val responseBuilder: ResponseBuilder,
) : AccessDeniedHandler {

    /** 已登录但权限不足时返回 403 JSON。 */
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        val entity = responseBuilder.forbidden().build()
        response.status = entity.statusCode.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()
        response.writer.write(JSON.toJSONString(entity.body))
        response.writer.flush()
    }
}