package top.foxball.nekobackend.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "security.jwt")
class JwtProperties {
    /** JWT 签名密钥。 */
    var secret: String = ""

    /** 签发方标识。 */
    var issuer: String = "NekoBackend"

    /** access token 有效期，单位秒。 */
    var accessTokenTtlSeconds: Long = 7200
}
