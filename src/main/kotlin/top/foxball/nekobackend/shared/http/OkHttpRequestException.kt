package top.foxball.nekobackend.shared.http

/** 远程 HTTP 调用返回非 2xx 状态码时抛出的异常。 */
class OkHttpRequestException(
    val statusCode: Int,
    val responseBody: String,
    message: String,
) : RuntimeException(message)
