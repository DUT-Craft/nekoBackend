package top.foxball.nekobackend.security.jwt

import java.time.Instant

/**
 * JWT 解析后的标准载荷数据。
 *
 * 这里只保留认证和会话校验需要的信息，不放敏感字段。
 */
data class JwtClaims(
    /** 用户 ID。 */
    val userId: Long,
    /** 登录用户名。 */
    val username: String,
    /** 权限快照。 */
    val authorities: List<String>,
    /** 过期时间。 */
    val expiresAt: Instant,
)
