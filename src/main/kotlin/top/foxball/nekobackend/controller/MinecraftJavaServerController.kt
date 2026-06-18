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

        return builder.ok()
            .data(minecraftJavaServerService.create(principal.userId, request))
            .build()
    }

    @GetMapping
    fun listMine(
        authentication: Authentication,
        @RequestParam(required = false) version: String?,
        @RequestParam(required = false) serverType: MinecraftJavaServerType?,
        @RequestParam(required = false) status: MinecraftJavaServerStatus?,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal

        return builder.ok()
            .data(
                minecraftJavaServerService.findMine(
                    userId = principal.userId,
                    version = version,
                    serverType = serverType,
                    status = status,
                )
            )
            .build()
    }

    @GetMapping("/{id}")
    fun getById(
        authentication: Authentication,
        @PathVariable id: Long,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal

        return builder.ok()
            .data(minecraftJavaServerService.findById(principal.userId, id))
            .build()
    }

    @PutMapping("/{id}")
    fun update(
        authentication: Authentication,
        @PathVariable id: Long,
        @RequestBody request: MinecraftJavaServerUpdateRequest,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal

        return builder.ok()
            .data(minecraftJavaServerService.update(principal.userId, id, request))
            .build()
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        authentication: Authentication,
        @PathVariable id: Long,
        @RequestBody request: MinecraftJavaServerStatusUpdateRequest,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal

        return builder.ok()
            .data(minecraftJavaServerService.updateStatus(principal.userId, id, request))
            .build()
    }

    @DeleteMapping("/{id}")
    fun delete(
        authentication: Authentication,
        @PathVariable id: Long,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        minecraftJavaServerService.delete(principal.userId, id)

        return builder.ok()
            .data(mapOf("deleted" to true))
            .build()
    }
}
