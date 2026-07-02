package top.foxball.nekobackend.security.permission

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

/** 用户组方法级授权的核心判断逻辑。 */
@Component
class GroupAuthorizationService {
    /** 判断当前认证用户是否属于指定用户组。 */
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
