package top.foxball.nekobackend.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import top.foxball.nekobackend.datasource.jdbc.MinecraftJavaServerStatus
import top.foxball.nekobackend.datasource.jdbc.MinecraftJavaServerType
import top.foxball.nekobackend.security.AuthPrincipal
import top.foxball.nekobackend.service.MinecraftJavaServerCreateRequest
import top.foxball.nekobackend.service.MinecraftJavaServerService
import top.foxball.nekobackend.service.MinecraftJavaServerStatusUpdateRequest
import top.foxball.nekobackend.service.MinecraftJavaServerUpdateRequest
import top.foxball.nekobackend.shared.Response
import top.foxball.nekobackend.shared.ResponseBuilder
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/minecraft/java/servers")
class MinecraftJavaServerController(
    private val minecraftJavaServerService: MinecraftJavaServerService,
    private val builder: ResponseBuilder,
) {

    @PostMapping
    fun create(
        authentication: Authentication,
        @RequestBody request: MinecraftJavaServerCreateRequest,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val server = minecraftJavaServerService.create(principal.userId, request)

        data class Response(
            val id: Long?,
            val userId: Long?,
            val name: String?,
            val description: String?,
            val version: String?,
            val serverType: MinecraftJavaServerType?,
            val status: MinecraftJavaServerStatus?,
            val address: String?,
            val port: Int?,
            val motd: String?,
            val maxPlayers: Int?,
            val onlineMode: Boolean?,
            val whitelistEnabled: Boolean?,
            val iconUrl: String?,
            val virtualMachineId: Long?,
            val lastCheckedAt: LocalDateTime?,
            val createdAt: LocalDateTime?,
            val updatedAt: LocalDateTime?,
            val remark: String?,
        )

        val rs = Response(
            id = server.id,
            userId = server.userId,
            name = server.name,
            description = server.description,
            version = server.version,
            serverType = server.serverType,
            status = server.status,
            address = server.address,
            port = server.port,
            motd = server.motd,
            maxPlayers = server.maxPlayers,
            onlineMode = server.onlineMode,
            whitelistEnabled = server.whitelistEnabled,
            iconUrl = server.iconUrl,
            virtualMachineId = server.virtualMachineId,
            lastCheckedAt = server.lastCheckedAt,
            createdAt = server.createdAt,
            updatedAt = server.updatedAt,
            remark = server.remark,
        )

        return builder.ok().data(rs).build()
    }

    @GetMapping
    fun listMine(
        authentication: Authentication,
        @RequestParam(required = false) version: String?,
        @RequestParam(required = false) serverType: MinecraftJavaServerType?,
        @RequestParam(required = false) status: MinecraftJavaServerStatus?,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val servers = minecraftJavaServerService.findMine(
            userId = principal.userId,
            version = version,
            serverType = serverType,
            status = status,
        )

        data class Response(
            val id: Long?,
            val userId: Long?,
            val name: String?,
            val description: String?,
            val version: String?,
            val serverType: MinecraftJavaServerType?,
            val status: MinecraftJavaServerStatus?,
            val address: String?,
            val port: Int?,
            val motd: String?,
            val maxPlayers: Int?,
            val onlineMode: Boolean?,
            val whitelistEnabled: Boolean?,
            val iconUrl: String?,
            val virtualMachineId: Long?,
            val lastCheckedAt: LocalDateTime?,
            val createdAt: LocalDateTime?,
            val updatedAt: LocalDateTime?,
            val remark: String?,
        )

        val rs = servers.map {
            Response(
                id = it.id,
                userId = it.userId,
                name = it.name,
                description = it.description,
                version = it.version,
                serverType = it.serverType,
                status = it.status,
                address = it.address,
                port = it.port,
                motd = it.motd,
                maxPlayers = it.maxPlayers,
                onlineMode = it.onlineMode,
                whitelistEnabled = it.whitelistEnabled,
                iconUrl = it.iconUrl,
                virtualMachineId = it.virtualMachineId,
                lastCheckedAt = it.lastCheckedAt,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
                remark = it.remark,
            )
        }

        return builder.ok().data(rs).build()
    }

    @GetMapping("/{id}")
    fun getById(
        authentication: Authentication,
        @PathVariable id: Long,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val server = minecraftJavaServerService.findById(principal.userId, id)

        data class Response(
            val id: Long?,
            val userId: Long?,
            val name: String?,
            val description: String?,
            val version: String?,
            val serverType: MinecraftJavaServerType?,
            val status: MinecraftJavaServerStatus?,
            val address: String?,
            val port: Int?,
            val motd: String?,
            val maxPlayers: Int?,
            val onlineMode: Boolean?,
            val whitelistEnabled: Boolean?,
            val iconUrl: String?,
            val virtualMachineId: Long?,
            val lastCheckedAt: LocalDateTime?,
            val createdAt: LocalDateTime?,
            val updatedAt: LocalDateTime?,
            val remark: String?,
        )

        val rs = Response(
            id = server.id,
            userId = server.userId,
            name = server.name,
            description = server.description,
            version = server.version,
            serverType = server.serverType,
            status = server.status,
            address = server.address,
            port = server.port,
            motd = server.motd,
            maxPlayers = server.maxPlayers,
            onlineMode = server.onlineMode,
            whitelistEnabled = server.whitelistEnabled,
            iconUrl = server.iconUrl,
            virtualMachineId = server.virtualMachineId,
            lastCheckedAt = server.lastCheckedAt,
            createdAt = server.createdAt,
            updatedAt = server.updatedAt,
            remark = server.remark,
        )

        return builder.ok().data(rs).build()
    }

    @PutMapping("/{id}")
    fun update(
        authentication: Authentication,
        @PathVariable id: Long,
        @RequestBody request: MinecraftJavaServerUpdateRequest,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val server = minecraftJavaServerService.update(principal.userId, id, request)

        data class Response(
            val id: Long?,
            val userId: Long?,
            val name: String?,
            val description: String?,
            val version: String?,
            val serverType: MinecraftJavaServerType?,
            val status: MinecraftJavaServerStatus?,
            val address: String?,
            val port: Int?,
            val motd: String?,
            val maxPlayers: Int?,
            val onlineMode: Boolean?,
            val whitelistEnabled: Boolean?,
            val iconUrl: String?,
            val virtualMachineId: Long?,
            val lastCheckedAt: LocalDateTime?,
            val createdAt: LocalDateTime?,
            val updatedAt: LocalDateTime?,
            val remark: String?,
        )

        val rs = Response(
            id = server.id,
            userId = server.userId,
            name = server.name,
            description = server.description,
            version = server.version,
            serverType = server.serverType,
            status = server.status,
            address = server.address,
            port = server.port,
            motd = server.motd,
            maxPlayers = server.maxPlayers,
            onlineMode = server.onlineMode,
            whitelistEnabled = server.whitelistEnabled,
            iconUrl = server.iconUrl,
            virtualMachineId = server.virtualMachineId,
            lastCheckedAt = server.lastCheckedAt,
            createdAt = server.createdAt,
            updatedAt = server.updatedAt,
            remark = server.remark,
        )

        return builder.ok().data(rs).build()
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        authentication: Authentication,
        @PathVariable id: Long,
        @RequestBody request: MinecraftJavaServerStatusUpdateRequest,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val server = minecraftJavaServerService.updateStatus(principal.userId, id, request)

        data class Response(
            val id: Long?,
            val userId: Long?,
            val name: String?,
            val description: String?,
            val version: String?,
            val serverType: MinecraftJavaServerType?,
            val status: MinecraftJavaServerStatus?,
            val address: String?,
            val port: Int?,
            val motd: String?,
            val maxPlayers: Int?,
            val onlineMode: Boolean?,
            val whitelistEnabled: Boolean?,
            val iconUrl: String?,
            val virtualMachineId: Long?,
            val lastCheckedAt: LocalDateTime?,
            val createdAt: LocalDateTime?,
            val updatedAt: LocalDateTime?,
            val remark: String?,
        )

        val rs = Response(
            id = server.id,
            userId = server.userId,
            name = server.name,
            description = server.description,
            version = server.version,
            serverType = server.serverType,
            status = server.status,
            address = server.address,
            port = server.port,
            motd = server.motd,
            maxPlayers = server.maxPlayers,
            onlineMode = server.onlineMode,
            whitelistEnabled = server.whitelistEnabled,
            iconUrl = server.iconUrl,
            virtualMachineId = server.virtualMachineId,
            lastCheckedAt = server.lastCheckedAt,
            createdAt = server.createdAt,
            updatedAt = server.updatedAt,
            remark = server.remark,
        )

        return builder.ok().data(rs).build()
    }

    @DeleteMapping("/{id}")
    fun delete(
        authentication: Authentication,
        @PathVariable id: Long,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        minecraftJavaServerService.delete(principal.userId, id)

        data class Response(
            val deleted: Boolean,
        )

        val rs = Response(
            deleted = true,
        )

        return builder.ok().data(rs).build()
    }
}
