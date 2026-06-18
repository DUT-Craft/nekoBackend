package top.foxball.nekobackend.shared.http

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/** OkHttp 客户端连接池和超时配置。 */
@ConfigurationProperties(prefix = "neko.okhttp")
data class OkHttpProperties(
    var connectTimeout: Duration = Duration.ofSeconds(5),
    var readTimeout: Duration = Duration.ofSeconds(15),
    var writeTimeout: Duration = Duration.ofSeconds(15),
    var callTimeout: Duration = Duration.ofSeconds(30),
    var maxIdleConnections: Int = 20,
    var keepAliveDuration: Duration = Duration.ofMinutes(5),
    var retryOnConnectionFailure: Boolean = true,
)
