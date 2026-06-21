package top.foxball.nekobackend.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import top.foxball.nekobackend.security.permission.RequireRole
import top.foxball.nekobackend.service.PveManagementService
import top.foxball.nekobackend.service.PveUserCreateRequest
import top.foxball.nekobackend.service.PveUserUpdateRequest
import top.foxball.nekobackend.shared.Response
import top.foxball.nekobackend.shared.ResponseBuilder

@RestController
@RequestMapping("/api/admin/pve-users")
@RequireRole("ADMIN")
class AdminPveUserController(
    private val pveManagementService: PveManagementService,
    private val builder: ResponseBuilder,
) {

    @PostMapping
    fun create(@RequestBody request: PveUserCreateRequest): ResponseEntity<Response> {
        val pveUser = pveManagementService.createPveUser(request)

        data class Response(
            val id: Long?,
            val userId: Long?,
            val username: String?,
            val address: String?,
            val port: String?,
            val pam: String?,
            val email: String?,
            val createdAt: Long?,
            val updatedAt: Long?,
            val status: String?,
            val hasPassword: Boolean,
            val hasApiToken: Boolean,
        )

        val rs = Response(
            id = pveUser.id,
            userId = pveUser.userId,
            username = pveUser.username,
            address = pveUser.address,
            port = pveUser.port,
            pam = pveUser.pam,
            email = pveUser.email,
            createdAt = pveUser.createdAt,
            updatedAt = pveUser.updatedAt,
            status = pveUser.status,
            hasPassword = pveUser.hasPassword,
            hasApiToken = pveUser.hasApiToken,
        )

        return builder.ok().data(rs).build()
    }

    @GetMapping
    fun list(
        @RequestParam(required = false) userId: Long?,
        @RequestParam(required = false) username: String?,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) address: String?,
    ): ResponseEntity<Response> {
        val pveUsers = pveManagementService.listPveUsers(
            userId = userId,
            username = username,
            email = email,
            status = status,
            address = address,
        )

        data class Response(
            val id: Long?,
            val userId: Long?,
            val username: String?,
            val address: String?,
            val port: String?,
            val pam: String?,
            val email: String?,
            val createdAt: Long?,
            val updatedAt: Long?,
            val status: String?,
            val hasPassword: Boolean,
            val hasApiToken: Boolean,
        )

        val rs = pveUsers.map {
            Response(
                id = it.id,
                userId = it.userId,
                username = it.username,
                address = it.address,
                port = it.port,
                pam = it.pam,
                email = it.email,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
                status = it.status,
                hasPassword = it.hasPassword,
                hasApiToken = it.hasApiToken,
            )
        }

        return builder.ok().data(rs).build()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Response> {
        val pveUser = pveManagementService.getPveUser(id)

        data class Response(
            val id: Long?,
            val userId: Long?,
            val username: String?,
            val address: String?,
            val port: String?,
            val pam: String?,
            val email: String?,
            val createdAt: Long?,
            val updatedAt: Long?,
            val status: String?,
            val hasPassword: Boolean,
            val hasApiToken: Boolean,
        )

        val rs = Response(
            id = pveUser.id,
            userId = pveUser.userId,
            username = pveUser.username,
            address = pveUser.address,
            port = pveUser.port,
            pam = pveUser.pam,
            email = pveUser.email,
            createdAt = pveUser.createdAt,
            updatedAt = pveUser.updatedAt,
            status = pveUser.status,
            hasPassword = pveUser.hasPassword,
            hasApiToken = pveUser.hasApiToken,
        )

        return builder.ok().data(rs).build()
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: PveUserUpdateRequest,
    ): ResponseEntity<Response> {
        val pveUser = pveManagementService.updatePveUser(id, request)

        data class Response(
            val id: Long?,
            val userId: Long?,
            val username: String?,
            val address: String?,
            val port: String?,
            val pam: String?,
            val email: String?,
            val createdAt: Long?,
            val updatedAt: Long?,
            val status: String?,
            val hasPassword: Boolean,
            val hasApiToken: Boolean,
        )

        val rs = Response(
            id = pveUser.id,
            userId = pveUser.userId,
            username = pveUser.username,
            address = pveUser.address,
            port = pveUser.port,
            pam = pveUser.pam,
            email = pveUser.email,
            createdAt = pveUser.createdAt,
            updatedAt = pveUser.updatedAt,
            status = pveUser.status,
            hasPassword = pveUser.hasPassword,
            hasApiToken = pveUser.hasApiToken,
        )

        return builder.ok().data(rs).build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Response> {
        pveManagementService.deletePveUser(id)

        data class Response(
            val deleted: Boolean,
        )

        val rs = Response(
            deleted = true,
        )

        return builder.ok().data(rs).build()
    }
}
