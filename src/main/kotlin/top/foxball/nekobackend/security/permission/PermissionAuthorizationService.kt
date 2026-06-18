package top.foxball.nekobackend.security.permission

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

/** 自定义权限注解的核心判断逻辑。 */
@Component
class PermissionAuthorizationService {

    /** 判断当前认证用户是否满足指定权限要求。 */
    fun hasPermissions(
        authentication: Authentication?,
        permissions: Array<out String>,
        mode: AuthMatchMode,
    ): Boolean {
        if (!isAuthenticated(authentication)) return false

        val requiredPermissions = permissions
            .mapNotNull { it.trim().takeIf { permission -> permission.isNotBlank() } }
            .toSet()
        if (requiredPermissions.isEmpty()) return false

        val authorities = authentication.authorityNames()
        return matches(requiredPermissions, authorities, mode)
    }

    /** 判断当前认证用户是否满足指定角色要求。 */
    fun hasRoles(
        authentication: Authentication?,
        roles: Array<out String>,
        mode: AuthMatchMode,
    ): Boolean {
        if (!isAuthenticated(authentication)) return false

        val requiredRoles = roles
            .mapNotNull { normalizeRole(it) }
            .toSet()
        if (requiredRoles.isEmpty()) return false

        val authorities = authentication.authorityNames()
        return matches(requiredRoles, authorities, mode)
    }

    private fun isAuthenticated(authentication: Authentication?): Boolean {
        return authentication != null &&
            authentication.isAuthenticated &&
            authentication !is AnonymousAuthenticationToken
    }

    private fun normalizeRole(role: String): String? {
        val trimmed = role.trim()
        if (trimmed.isBlank()) return null

        val roleCode = if (trimmed.uppercase().startsWith(ROLE_PREFIX)) {
            trimmed.substring(ROLE_PREFIX.length)
        } else {
            trimmed
        }

        return ROLE_PREFIX + roleCode.uppercase()
    }

    private fun Authentication?.authorityNames(): Set<String> {
        return this?.authorities
            ?.mapNotNull { it.authority?.trim()?.takeIf { authority -> authority.isNotBlank() } }
            ?.toSet()
            ?: emptySet()
    }

    private fun matches(
        required: Set<String>,
        actual: Set<String>,
        mode: AuthMatchMode,
    ): Boolean {
        return when (mode) {
            AuthMatchMode.ANY -> required.any { it in actual }
            AuthMatchMode.ALL -> required.all { it in actual }
        }
    }

    private companion object {
        private const val ROLE_PREFIX = "ROLE_"
    }
}
