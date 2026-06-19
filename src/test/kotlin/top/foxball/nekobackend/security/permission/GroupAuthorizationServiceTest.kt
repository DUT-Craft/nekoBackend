package top.foxball.nekobackend.security.permission

import org.junit.jupiter.api.Test
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GroupAuthorizationServiceTest {
    private val service = GroupAuthorizationService()

    @Test
    fun `role all mode requires every group`() {
        val authentication = authentication("ROLE_ADMIN", "ROLE_OPERATOR")

        assertTrue(
            service.hasRoles(
                authentication,
                arrayOf("ADMIN", "OPERATOR"),
                AuthMatchMode.ALL,
            )
        )
        assertFalse(
            service.hasRoles(
                authentication,
                arrayOf("ADMIN", "MODERATOR"),
                AuthMatchMode.ALL,
            )
        )
    }

    @Test
    fun `role any mode requires one group`() {
        val authentication = authentication("ROLE_OPERATOR")

        assertTrue(
            service.hasRoles(
                authentication,
                arrayOf("ADMIN", "OPERATOR"),
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
    }

    private fun authentication(vararg authorities: String): Authentication {
        return UsernamePasswordAuthenticationToken(
            "user",
            null,
            authorities.map { SimpleGrantedAuthority(it) },
        )
    }
}
