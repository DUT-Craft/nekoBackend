package top.foxball.nekobackend.datasource.redis

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

@RedisHash("email_code")
class EmailCode {

    @Id
    var id: String? = null

    var code: String? = null
    var email: String? = null
    var userName: String? = null
    var userAgent: String? = null

    @TimeToLive
    var ttlSeconds: Long = 600
}
