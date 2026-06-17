package top.foxball.nekobackend.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import top.foxball.nekobackend.datasource.jdbc.Status
import top.foxball.nekobackend.datasource.jdbc.UserRepository

@Service
class NekoUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    /**
     * 通过用户名加载用户，并把角色、权限统一映射为 GrantedAuthority。
     *
     * 角色统一加 ROLE_ 前缀，权限直接使用数据库中的 code。
     */
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        // 数据库角色转成 Spring Security 角色权限。
        val roleAuthorities = user.roles
            .mapNotNull { it.code?.trim()?.takeIf { code -> code.isNotBlank() } }
            .map { code -> SimpleGrantedAuthority("ROLE_${code.uppercase()}") }

        // 用户直接拥有的权限，保持原始 code 不变。
        val directPermissions = user.permissions
            .mapNotNull { it.code?.trim()?.takeIf { code -> code.isNotBlank() } }
            .map { code -> SimpleGrantedAuthority(code) }

        // 角色挂载的权限也一起合并进来。
        val rolePermissions = user.roles
            .flatMap { it.permissions }
            .mapNotNull { it.code?.trim()?.takeIf { code -> code.isNotBlank() } }
            .map { code -> SimpleGrantedAuthority(code) }

        // 去重，避免同一个权限重复出现。
        val authorities = (roleAuthorities + directPermissions + rolePermissions)
            .distinctBy { it.authority }

        return AuthPrincipal(
            userId = user.id ?: throw UsernameNotFoundException("User id is missing: $username"),
            loginUsername = user.username,
            passwordHash = user.password,
            grantedAuthorities = authorities,
            active = user.status == Status.ACTIVE,
        )
    }
}
