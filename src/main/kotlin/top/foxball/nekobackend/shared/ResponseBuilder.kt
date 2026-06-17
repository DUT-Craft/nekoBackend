package top.foxball.nekobackend.shared

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

/**
 * 响应构建器组件
 * 提供流式 API 来构建统一的 HTTP 响应，支持各种 HTTP 状态码和自定义消息
 * 使用 Builder 模式，允许链式调用设置消息、数据和头部信息
 */
@Component
class ResponseBuilder {

    // ===================== 入口方法 (Entry Points) =====================

    /**
     * 创建 200 OK 响应构建器
     * @return Builder 实例
     */
    fun ok(): Builder = Builder(ResponseCode.OK)

    /**
     * 创建 404 Not Found 响应构建器
     * @return Builder 实例
     */
    fun notFound(): Builder = Builder(ResponseCode.NOT_FOUND)

    /**
     * 创建 400 Bad Request 响应构建器
     * @return Builder 实例
     */
    fun badRequest(): Builder = Builder(ResponseCode.BAD_REQUEST)

    /**
     * 创建 403 Forbidden 响应构建器
     * @return Builder 实例
     */
    fun forbidden(): Builder = Builder(ResponseCode.FORBIDDEN)

    /**
     * 创建 401 Unauthorized 响应构建器
     * @return Builder 实例
     */
    fun unauthorized(): Builder = Builder(ResponseCode.UNAUTHORIZED)

    /**
     * 创建 429 Too Many Requests 响应构建器
     * @return Builder 实例
     */
    fun tooManyRequests(): Builder = Builder(ResponseCode.TOO_MANY_REQUESTS)

    /**
     * 创建 500 Internal Server Error 响应构建器
     * @return Builder 实例
     */
    fun exception(): Builder = Builder(ResponseCode.INTERNAL_SERVER_ERROR)

    /**
     * 创建 503 Service Unavailable 响应构建器
     * @return Builder 实例
     */
    fun serviceUnavailable(): Builder = Builder(ResponseCode.SERVICE_UNAVAILABLE)

    /**
     * 创建 418 I'm a teapot 响应构建器（用于特殊场景）
     * @return Builder 实例
     */
    fun teapot(): Builder = Builder(ResponseCode.IM_A_TEAPOT)

    /**
     * 自定义状态码入口
     * @param status HTTP 状态码
     * @return Builder 实例
     */
    fun status(status: HttpStatus): Builder = Builder(status)

    // ===================== 内部建造者类 (Inner Builder) =====================

    /**
     * 响应构建者内部类
     * 用于逐步构建响应对象，支持链式调用
     */
    inner class Builder {
        /** 响应状态码数值 */
        private var status: Int

        /** 默认响应消息 */
        private var defaultMessage: String

        /** 自定义响应消息 */
        private var customMessage: String? = null

        /** 响应数据体 */
        private var data: Any? = null

        /** HTTP 响应头 */
        private val headers = HttpHeaders()

        // 构造函数：基于预定义的 Response 枚举
        constructor(responseCode: ResponseCode) {
            this.status = responseCode.code
            this.defaultMessage = responseCode.message
        }

        // 构造函数：基于 Spring HttpStatus
        constructor(httpStatus: HttpStatus) {
            this.status = httpStatus.value()
            this.defaultMessage = httpStatus.reasonPhrase
        }

        /**
         * 设置返回消息
         * @param message 自定义响应消息
         * @return Builder 实例（支持链式调用）
         */
        fun message(message: String?) = apply {
            this.customMessage = message
        }

        /**
         * 设置返回数据 (支持任意对象，内部会自动转 Map)
         * @param data 响应数据对象
         * @return Builder 实例（支持链式调用）
         */
        fun data(data: Any?) = apply {
            this.data = data
        }

        /**
         * 添加单个 Header
         * @param key Header 名称
         * @param value Header 值
         * @return Builder 实例（支持链式调用）
         */
        fun header(key: String, value: String) = apply {
            this.headers.add(key, value)
        }

        /**
         * 批量添加 Headers
         * @param httpHeaders HTTP 头部信息
         * @return Builder 实例（支持链式调用）
         */
        fun headers(httpHeaders: HttpHeaders) = apply {
            this.headers.addAll(httpHeaders)
        }

        /**
         * 特有方法：设置重试时间 (针对 429 Too Many Requests)
         * @param seconds 重试等待时间（秒）
         * @return Builder 实例（支持链式调用）
         */
        fun retryAfter(seconds: Long) = apply {
            this.headers.add("Retry-After", seconds.toString())
        }

        /**
         * 最终构建方法
         * @return ResponseEntity<Response> 完整的 HTTP 响应实体
         */
        fun build(): ResponseEntity<Response> { // 【关键修改点：返回值】
            val finalData = this.data ?: HashMap<String, Any?>()

            val responseBody = Response(
                status = this.status,
                message = this.customMessage ?: this.defaultMessage,
                data = finalData
            )

            return ResponseEntity
                .status(this.status)
                .headers(this.headers)
                .body(responseBody)
        }
    }

    /**
     * 定义标准响应类型及其对应的状态码和默认消息
     * 包含常用的 HTTP 状态码，简化响应构建流程
     */
    enum class ResponseCode(val code: Int, val message: String) {
        OK(200, "OK"),
        NOT_FOUND(404, "Not Found"),
        UNAUTHORIZED(401, "Unauthorized"),
        FORBIDDEN(403, "Forbidden"),
        BAD_REQUEST(400, "Bad Request"),
        TOO_MANY_REQUESTS(429, "Too Many Requests"),
        INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
        IM_A_TEAPOT(418, "I'm a teapot"),
        SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    }
}