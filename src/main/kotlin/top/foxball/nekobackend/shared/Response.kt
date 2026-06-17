package top.foxball.nekobackend.shared

/**
 * 统一 API 响应数据类
 * 用于封装所有 HTTP 请求的响应数据，保持接口返回格式的一致性
 * @param status HTTP 状态码
 * @param message 响应消息
 * @param data 响应数据体，可以是任意对象或 null
 */
data class Response(
    val status: Int,
    val message: String,
    val data: Any?
)