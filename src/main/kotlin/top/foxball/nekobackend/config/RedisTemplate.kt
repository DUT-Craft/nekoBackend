package top.foxball.nekobackend.config

import com.alibaba.fastjson2.support.spring6.data.redis.GenericFastJsonRedisSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class RedisTemplate {
    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory?): RedisTemplate<*, *> {
        val redisTemplate: RedisTemplate<*, *> = RedisTemplate<Any?, Any?>()
        redisTemplate.connectionFactory = redisConnectionFactory

        val fastJsonRedisSerializer = GenericFastJsonRedisSerializer()
        redisTemplate.defaultSerializer = fastJsonRedisSerializer //设置默认的Serialize，包含 keySerializer & valueSerializer

        return redisTemplate
    }
}