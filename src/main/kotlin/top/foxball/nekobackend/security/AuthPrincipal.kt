package top.foxball.nekobackend.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Spring Security 使用的当前登录主体。
 *
 * 这个对象把数据库中的用户信息转换成 SecurityContext 可识别的身份数据，
 * 供认证、授权和控制器获取当前登录用户时使用。
 */
class AuthPrincipal(
    /** 当前登录用户的数据库主键。 */
    val userId: Long,
    /** 登录用户名。 */
    private val loginUsername: String,
    /** BCrypt 加密后的密码摘要。 */
    private val passwordHash: String,
    /** 用户拥有的角色和权限。 */
    private val grantedAuthorities: Collection<GrantedAuthority>,
    /** 账号是否处于可用状态。 */
    private val active: Boolean,
) : UserDetails {

    /** 返回当前用户的全部权限。 */
    override fun getAuthorities(): Collection<GrantedAuthority> = grantedAuthorities

    /** 返回密码摘要，仅供 Spring Security 校验使用。 */
    override fun getPassword(): String = passwordHash

    /** 返回登录用户名。 */
    override fun getUsername(): String = loginUsername

    /** 当前实现不使用账号过期概念。 */
    override fun isAccountNonExpired(): Boolean = true

    /** 账号未被禁用时才允许登录和认证通过。 */
    override fun isAccountNonLocked(): Boolean = active

    /** 当前实现不使用凭证过期概念。 */
    override fun isCredentialsNonExpired(): Boolean = true

    /** 账号是否启用。 */
    override fun isEnabled(): Boolean = active
}
