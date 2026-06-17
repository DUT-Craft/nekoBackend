package top.foxball.nekobackend.security.jwt

import com.alibaba.fastjson2.JSON
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import top.foxball.nekobackend.handlder.TokenInvalidException
import java.time.Duration

@Service
class JwtSessionService(
    private val stringRedisTemplate: StringRedisTemplate,
    private val jwtProperties: JwtProperties,
) {

    companion object {
        private const val MAX_USER_TOKENS = 3L
    }

    /** 登录成功后写入 Redis，会话数据包含 token、用户 ID 和 User-Agent。 */
    fun save(
        token: String,
        userId: Long,
        userAgent: String?,
    ) {
        val tokenKey = tokenKey(token)
        val userTokensKey = userTokensKey(userId)
        val ttl = Duration.ofSeconds(jwtProperties.accessTokenTtlSeconds)

        val session = mapOf(
            "userId" to userId,
            "userAgent" to normalizeUserAgent(userAgent),
        )

        stringRedisTemplate.opsForValue().set(tokenKey, JSON.toJSONString(session), ttl)
        stringRedisTemplate.opsForZSet().add(userTokensKey, token, System.currentTimeMillis().toDouble())
        stringRedisTemplate.expire(userTokensKey, ttl)
        trimUserTokens(userTokensKey)
    }

    /** 每次请求都检查 token 是否仍在 Redis 中，并校验 User-Agent 和用户 ID。 */
    fun validate(
        token: String,
        userId: Long,
        userAgent: String,
    ) {
        val sessionJson = stringRedisTemplate.opsForValue().get(tokenKey(token))
            ?: throw TokenInvalidException()

        val session = try {
            JSON.parseObject(sessionJson)
        } catch (ex: Exception) {
            throw TokenInvalidException()
        }

        val storedUserId = session.getLong("userId") ?: throw TokenInvalidException()
        val storedUserAgent = session.getString("userAgent") ?: throw TokenInvalidException()

        if (storedUserId != userId) {
            throw TokenInvalidException()
        }
        if (storedUserAgent != normalizeUserAgent(userAgent)) {
            throw TokenInvalidException("Token 与 User-Agent 不匹配")
        }
    }

    /** 修改密码后撤销该用户当前保存的全部 token。 */
    fun revokeAll(userId: Long) {
        val userTokensKey = userTokensKey(userId)
        val tokens = stringRedisTemplate.opsForZSet().range(userTokensKey, 0, -1).orEmpty()
        val tokenKeys = tokens.map { tokenKey(it) }

        if (tokenKeys.isNotEmpty()) {
            stringRedisTemplate.delete(tokenKeys)
        }
        stringRedisTemplate.delete(userTokensKey)
    }

    /** 每个用户最多保留 3 个 token，超过后删除最早生成的 token。 */
    private fun trimUserTokens(userTokensKey: String) {
        val tokenCount = stringRedisTemplate.opsForZSet().zCard(userTokensKey) ?: return
        val overflow = tokenCount - MAX_USER_TOKENS
        if (overflow <= 0) return

        val tokensToRevoke = stringRedisTemplate.opsForZSet()
            .range(userTokensKey, 0, overflow - 1)
            .orEmpty()
        if (tokensToRevoke.isEmpty()) return

        stringRedisTemplate.delete(tokensToRevoke.map { tokenKey(it) })
        stringRedisTemplate.opsForZSet().remove(userTokensKey, *tokensToRevoke.toTypedArray())
    }

    /** 统一规范化 User-Agent，避免空值和前后空白造成误判。 */
    private fun normalizeUserAgent(userAgent: String?): String {
        return userAgent?.trim()?.takeIf { it.isNotBlank() } ?: "UNKNOWN"
    }

    /** 单个 token 的 Redis key。 */
    private fun tokenKey(token: String): String = "auth:token:$token"

    /** 某个用户的 token 集合 Redis key。 */
    private fun userTokensKey(userId: Long): String = "auth:user_tokens:$userId"
}
