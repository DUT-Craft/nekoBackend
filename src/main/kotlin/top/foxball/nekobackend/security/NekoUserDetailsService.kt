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
     * 通过用户名加载用户，并把用户组统一映射为 GrantedAuthority。
     *
     * 用户组统一加 ROLE_ 前缀。
     */
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        // 数据库用户组转成 Spring Security 角色权限。
        val authorities = user.roles
            .mapNotNull { it.code?.trim()?.takeIf { code -> code.isNotBlank() } }
            .map { code -> SimpleGrantedAuthority("ROLE_${code.uppercase()}") }
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
