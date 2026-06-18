package top.foxball.nekobackend.security.permission

import org.junit.jupiter.api.Test
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PermissionAuthorizationServiceTest {
    private val service = PermissionAuthorizationService()

    @Test
    fun `permission all mode requires every permission`() {
        val authentication = authentication("user:read", "user:update")

        assertTrue(
            service.hasPermissions(
                authentication,
                arrayOf("user:read", "user:update"),
                AuthMatchMode.ALL,
            )
        )
        assertFalse(
            service.hasPermissions(
                authentication,
                arrayOf("user:read", "user:delete"),
                AuthMatchMode.ALL,
            )
        )
    }

    @Test
    fun `permission any mode requires one permission`() {
        val authentication = authentication("profile:update")

        assertTrue(
            service.hasPermissions(
                authentication,
                arrayOf("profile:read", "profile:update"),
                AuthMatchMode.ANY,
            )
        )
    }

    @Test
    fun `role check supports role prefix and normalized code`() {
        val authentication = authentication("ROLE_ADMIN")

        assertTrue(service.hasRoles(authentication, arrayOf("ADMIN"), AuthMatchMode.ALL))
        assertTrue(service.hasRoles(authentication, arrayOf("ROLE_ADMIN"), AuthMatchMode.ALL))
        assertFalse(service.hasRoles(authentication, arrayOf("MODERATOR"), AuthMatchMode.ALL))
    }

    @Test
    fun `anonymous authentication is denied`() {
        val anonymous = AnonymousAuthenticationToken(
            "key",
            "anonymous",
            listOf(SimpleGrantedAuthority("ROLE_ANONYMOUS")),
        )

        assertFalse(service.hasRoles(anonymous, arrayOf("ADMIN"), AuthMatchMode.ALL))
        assertFalse(service.hasPermissions(anonymous, arrayOf("user:read"), AuthMatchMode.ALL))
    }

    private fun authentication(vararg authorities: String): Authentication {
        return UsernamePasswordAuthenticationToken(
            "user",
            null,
            authorities.map { SimpleGrantedAuthority(it) },
        )
    }
}
