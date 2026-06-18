package top.foxball.nekobackend.shared.http

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

/** 注册全局复用的 OkHttpClient。 */
@Configuration
@EnableConfigurationProperties(OkHttpProperties::class)
class OkHttpConfig {

    @Bean
    fun okHttpClient(properties: OkHttpProperties): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(properties.connectTimeout)
            .readTimeout(properties.readTimeout)
            .writeTimeout(properties.writeTimeout)
            .callTimeout(properties.callTimeout)
            .connectionPool(
                ConnectionPool(
                    properties.maxIdleConnections,
                    properties.keepAliveDuration.toMillis(),
                    TimeUnit.MILLISECONDS,
                )
            )
            .retryOnConnectionFailure(properties.retryOnConnectionFailure)
            .build()
    }
}
