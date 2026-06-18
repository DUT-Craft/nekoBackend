package top.foxball.nekobackend.shared.http

import okhttp3.RequestBody

/** OkHttp 封装层的通用请求模型。 */
data class OkHttpRequest(
    val method: String,
    val url: String,
    val query: Map<String, Any?> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val body: RequestBody? = null,
    val throwOnFailure: Boolean = true,
)

/** OkHttp 封装层的通用响应模型。 */
data class OkHttpResponse(
    val code: Int,
    val message: String,
    val headers: Map<String, List<String>>,
    val body: String,
) {
    val successful: Boolean
        get() = code in 200..299
}
