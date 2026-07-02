package top.foxball.nekobackend.security.jwt

import com.alibaba.fastjson2.JSON
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import top.foxball.nekobackend.handlder.TokenExpiredException
import top.foxball.nekobackend.handlder.TokenInvalidException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class JwtTokenService(
    private val properties: JwtProperties,
) {
    private val base64UrlEncoder = Base64.getUrlEncoder().withoutPadding()
    private val base64UrlDecoder = Base64.getUrlDecoder()

    /** 启动时校验密钥长度，避免使用弱 secret。 */
    @PostConstruct
    fun validateSecret() {
        val secretBytes = properties.secret.toByteArray(StandardCharsets.UTF_8)
        require(secretBytes.size >= 32) {
            "security.jwt.secret must be at least 32 bytes."
        }
    }

    /** 生成带签名的 JWT access token。 */
    fun createAccessToken(
        userId: Long,
        username: String,
        authorities: Collection<String>,
    ): String {
        val now = Instant.now()

        val header = mapOf(
            "alg" to "HS256",
            "typ" to "JWT",
        )

        val payload = mapOf(
            "iss" to properties.issuer,
            "sub" to username,
            "uid" to userId,
            "iat" to now.epochSecond,
            "exp" to now.plusSeconds(properties.accessTokenTtlSeconds).epochSecond,
            "authorities" to authorities.distinct(),
        )

        val signingInput = "${base64Json(header)}.${base64Json(payload)}"
        return "$signingInput.${sign(signingInput)}"
    }

    /** 解析并校验 JWT 的签名、签发方和过期时间。 */
    fun parseAndValidate(rawToken: String): JwtClaims {
        val token = rawToken.trim()
        val parts = token.split(".")
        if (parts.size != 3) {
            throw TokenInvalidException()
        }

        val signingInput = "${parts[0]}.${parts[1]}"
        val expectedSignature = sign(signingInput)
        val signatureMatched = MessageDigest.isEqual(
            expectedSignature.toByteArray(StandardCharsets.UTF_8),
            parts[2].toByteArray(StandardCharsets.UTF_8),
        )
        if (!signatureMatched) {
            throw TokenInvalidException()
        }

        val payloadJson = try {
            String(base64UrlDecoder.decode(parts[1]), StandardCharsets.UTF_8)
        } catch (ex: IllegalArgumentException) {
            throw TokenInvalidException()
        }

        val payload = try {
            JSON.parseObject(payloadJson)
        } catch (ex: Exception) {
            throw TokenInvalidException()
        }

        val issuer = payload.getString("iss") ?: throw TokenInvalidException()
        if (issuer != properties.issuer) {
            throw TokenInvalidException()
        }

        val expiresAtEpochSecond = payload.getLong("exp") ?: throw TokenInvalidException()
        if (Instant.now().epochSecond >= expiresAtEpochSecond) {
            throw TokenExpiredException()
        }

        val userId = payload.getLong("uid") ?: throw TokenInvalidException()
        val username = payload.getString("sub") ?: throw TokenInvalidException()
        val authorities = payload.getJSONArray("authorities")
            ?.map { it.toString() }
            ?: emptyList()

        return JwtClaims(
            userId = userId,
            username = username,
            authorities = authorities,
            expiresAt = Instant.ofEpochSecond(expiresAtEpochSecond),
        )
    }

    /** 将对象序列化后编码成 Base64Url。 */
    private fun base64Json(value: Any): String {
        return base64UrlEncoder.encodeToString(
            JSON.toJSONString(value).toByteArray(StandardCharsets.UTF_8)
        )
    }

    /** 生成 HMAC-SHA256 签名。 */
    private fun sign(signingInput: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val key = SecretKeySpec(
            properties.secret.toByteArray(StandardCharsets.UTF_8),
            "HmacSHA256",
        )
        mac.init(key)
        return base64UrlEncoder.encodeToString(
            mac.doFinal(signingInput.toByteArray(StandardCharsets.UTF_8))
        )
    }
}
