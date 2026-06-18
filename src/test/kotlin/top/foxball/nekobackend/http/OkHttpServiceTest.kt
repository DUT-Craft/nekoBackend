package top.foxball.nekobackend.http

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import okhttp3.OkHttpClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.foxball.nekobackend.shared.http.OkHttpRequestException
import top.foxball.nekobackend.shared.http.OkHttpService
import java.net.InetSocketAddress
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OkHttpServiceTest {
    private lateinit var server: HttpServer
    private lateinit var service: OkHttpService

    @BeforeEach
    fun setUp() {
        server = HttpServer.create(InetSocketAddress(0), 0)
        service = OkHttpService(OkHttpClient())
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun `get sends query parameters and reads response body`() {
        server.createContext("/users") { exchange ->
            val query = exchange.requestURI.rawQuery
            exchange.writeJson(200, """{"query":"$query"}""")
        }

        val response = service.get(
            url = baseUrl("/users"),
            query = mapOf("name" to "neko", "tag" to listOf("a", "b")),
        )

        assertTrue(response.successful)
        assertEquals("""{"query":"name=neko&tag=a&tag=b"}""", response.body)
    }

    @Test
    fun `post json sends request body`() {
        server.createContext("/echo") { exchange ->
            exchange.writeJson(200, exchange.requestBody.bufferedReader().readText())
        }

        val response = service.postJson(
            url = baseUrl("/echo"),
            body = mapOf("name" to "neko"),
        )

        assertEquals("""{"name":"neko"}""", response.body)
    }

    @Test
    fun `non successful response throws exception by default`() {
        server.createContext("/failed") { exchange ->
            exchange.writeJson(500, """{"message":"failed"}""")
        }

        val exception = assertFailsWith<OkHttpRequestException> {
            service.get(baseUrl("/failed"))
        }

        assertEquals(500, exception.statusCode)
        assertEquals("""{"message":"failed"}""", exception.responseBody)
    }

    private fun baseUrl(path: String): String {
        return "http://127.0.0.1:${server.address.port}$path"
    }

    private fun HttpExchange.writeJson(status: Int, body: String) {
        responseHeaders.add("Content-Type", "application/json; charset=utf-8")
        val bytes = body.toByteArray(Charsets.UTF_8)
        sendResponseHeaders(status, bytes.size.toLong())
        responseBody.use { it.write(bytes) }
    }
}
