package top.foxball.nekobackend.shared.http

import com.alibaba.fastjson2.JSON
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.stereotype.Service

/** 对 OkHttpClient 的业务友好封装。 */
@Service
class OkHttpService(
    private val okHttpClient: OkHttpClient,
) {

    fun get(
        url: String,
        query: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
    ): OkHttpResponse {
        return exchange(
            OkHttpRequest(
                method = METHOD_GET,
                url = url,
                query = query,
                headers = headers,
            )
        )
    }

    fun delete(
        url: String,
        query: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
    ): OkHttpResponse {
        return exchange(
            OkHttpRequest(
                method = METHOD_DELETE,
                url = url,
                query = query,
                headers = headers,
            )
        )
    }

    fun postJson(
        url: String,
        body: Any?,
        query: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
    ): OkHttpResponse {
        return exchangeJson(METHOD_POST, url, body, query, headers)
    }

    fun putJson(
        url: String,
        body: Any?,
        query: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
    ): OkHttpResponse {
        return exchangeJson(METHOD_PUT, url, body, query, headers)
    }

    fun patchJson(
        url: String,
        body: Any?,
        query: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
    ): OkHttpResponse {
        return exchangeJson(METHOD_PATCH, url, body, query, headers)
    }

    fun <T : Any> getForObject(
        url: String,
        responseType: Class<T>,
        query: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
    ): T {
        return JSON.parseObject(get(url, query, headers).body, responseType)
    }

    fun <T : Any> postJsonForObject(
        url: String,
        body: Any?,
        responseType: Class<T>,
        query: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
    ): T {
        return JSON.parseObject(postJson(url, body, query, headers).body, responseType)
    }

    fun exchange(request: OkHttpRequest): OkHttpResponse {
        val okHttpRequest = request.toOkHttpRequest()

        okHttpClient.newCall(okHttpRequest).execute().use { response ->
            val responseBody = response.body.string()
            val result = OkHttpResponse(
                code = response.code,
                message = response.message,
                headers = response.headers.toMultimap(),
                body = responseBody,
            )

            if (request.throwOnFailure && !result.successful) {
                throw OkHttpRequestException(
                    statusCode = result.code,
                    responseBody = result.body,
                    message = "HTTP request failed: ${result.code} ${result.message}",
                )
            }

            return result
        }
    }

    private fun exchangeJson(
        method: String,
        url: String,
        body: Any?,
        query: Map<String, Any?>,
        headers: Map<String, String>,
    ): OkHttpResponse {
        return exchange(
            OkHttpRequest(
                method = method,
                url = url,
                query = query,
                headers = headers,
                body = JSON.toJSONString(body).toRequestBody(JSON_MEDIA_TYPE),
            )
        )
    }

    private fun OkHttpRequest.toOkHttpRequest(): Request {
        val builder = Request.Builder()
            .url(buildUrl(url, query))

        headers.forEach { (name, value) ->
            builder.header(name, value)
        }

        return when (method.uppercase()) {
            METHOD_GET -> builder.get().build()
            METHOD_DELETE -> if (body == null) builder.delete().build() else builder.delete(body).build()
            METHOD_POST -> builder.post(body ?: EMPTY_REQUEST_BODY).build()
            METHOD_PUT -> builder.put(body ?: EMPTY_REQUEST_BODY).build()
            METHOD_PATCH -> builder.patch(body ?: EMPTY_REQUEST_BODY).build()
            else -> builder.method(method.uppercase(), body).build()
        }
    }

    private fun buildUrl(
        url: String,
        query: Map<String, Any?>,
    ): HttpUrl {
        val builder = url.toHttpUrl().newBuilder()

        query.forEach { (name, value) ->
            when (value) {
                null -> Unit
                is Iterable<*> -> value
                    .filterNotNull()
                    .forEach { builder.addQueryParameter(name, it.toString()) }
                is Array<*> -> value
                    .filterNotNull()
                    .forEach { builder.addQueryParameter(name, it.toString()) }
                else -> builder.addQueryParameter(name, value.toString())
            }
        }

        return builder.build()
    }

    private companion object {
        private const val METHOD_GET = "GET"
        private const val METHOD_POST = "POST"
        private const val METHOD_PUT = "PUT"
        private const val METHOD_PATCH = "PATCH"
        private const val METHOD_DELETE = "DELETE"

        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private val EMPTY_REQUEST_BODY = ByteArray(0).toRequestBody(null)
    }
}
