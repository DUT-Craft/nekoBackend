package top.foxball.nekobackend.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import top.foxball.nekobackend.datasource.jdbc.VirtualMachineStatus
import top.foxball.nekobackend.security.permission.RequireRole
import top.foxball.nekobackend.service.PveManagementService
import top.foxball.nekobackend.service.VirtualMachineCreateRequest
import top.foxball.nekobackend.service.VirtualMachineStatusUpdateRequest
import top.foxball.nekobackend.service.VirtualMachineUpdateRequest
import top.foxball.nekobackend.shared.Response
import top.foxball.nekobackend.shared.ResponseBuilder

@RestController
@RequestMapping("/api/admin/virtual-machines")
@RequireRole("ADMIN")
class AdminVirtualMachineController(
    private val pveManagementService: PveManagementService,
    private val builder: ResponseBuilder,
) {

    @PostMapping
    fun create(@RequestBody request: VirtualMachineCreateRequest): ResponseEntity<Response> {
        val virtualMachine = pveManagementService.createVirtualMachine(request)

        data class Response(
            val id: Long?,
            val pveId: Long?,
            val name: String?,
            val description: String?,
            val status: VirtualMachineStatus?,
            val createdAt: Long?,
            val pveUserId: Long?,
            val systemUserName: String?,
            val hasSystemUserPassword: Boolean,
            val hasPublicKey: Boolean,
            val hasPrivateKey: Boolean,
        )

        val rs = Response(
            id = virtualMachine.id,
            pveId = virtualMachine.pveId,
            name = virtualMachine.name,
            description = virtualMachine.description,
            status = virtualMachine.status,
            createdAt = virtualMachine.createdAt,
            pveUserId = virtualMachine.pveUserId,
            systemUserName = virtualMachine.systemUserName,
            hasSystemUserPassword = virtualMachine.hasSystemUserPassword,
            hasPublicKey = virtualMachine.hasPublicKey,
            hasPrivateKey = virtualMachine.hasPrivateKey,
        )

        return builder.ok().data(rs).build()
    }

    @GetMapping
    fun list(
        @RequestParam(required = false) pveUserId: Long?,
        @RequestParam(required = false) pveId: Long?,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) status: VirtualMachineStatus?,
    ): ResponseEntity<Response> {
        val virtualMachines = pveManagementService.listVirtualMachines(
            pveUserId = pveUserId,
            pveId = pveId,
            name = name,
            status = status,
        )

        data class Response(
            val id: Long?,
            val pveId: Long?,
            val name: String?,
            val description: String?,
            val status: VirtualMachineStatus?,
            val createdAt: Long?,
            val pveUserId: Long?,
            val systemUserName: String?,
            val hasSystemUserPassword: Boolean,
            val hasPublicKey: Boolean,
            val hasPrivateKey: Boolean,
        )

        val rs = virtualMachines.map {
            Response(
                id = it.id,
                pveId = it.pveId,
                name = it.name,
                description = it.description,
                status = it.status,
                createdAt = it.createdAt,
                pveUserId = it.pveUserId,
                systemUserName = it.systemUserName,
                hasSystemUserPassword = it.hasSystemUserPassword,
                hasPublicKey = it.hasPublicKey,
                hasPrivateKey = it.hasPrivateKey,
            )
        }

        return builder.ok().data(rs).build()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Response> {
        val virtualMachine = pveManagementService.getVirtualMachine(id)

        data class Response(
            val id: Long?,
            val pveId: Long?,
            val name: String?,
            val description: String?,
            val status: VirtualMachineStatus?,
            val createdAt: Long?,
            val pveUserId: Long?,
            val systemUserName: String?,
            val hasSystemUserPassword: Boolean,
            val hasPublicKey: Boolean,
            val hasPrivateKey: Boolean,
        )

        val rs = Response(
            id = virtualMachine.id,
            pveId = virtualMachine.pveId,
            name = virtualMachine.name,
            description = virtualMachine.description,
            status = virtualMachine.status,
            createdAt = virtualMachine.createdAt,
            pveUserId = virtualMachine.pveUserId,
            systemUserName = virtualMachine.systemUserName,
            hasSystemUserPassword = virtualMachine.hasSystemUserPassword,
            hasPublicKey = virtualMachine.hasPublicKey,
            hasPrivateKey = virtualMachine.hasPrivateKey,
        )

        return builder.ok().data(rs).build()
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: VirtualMachineUpdateRequest,
    ): ResponseEntity<Response> {
        val virtualMachine = pveManagementService.updateVirtualMachine(id, request)

        data class Response(
            val id: Long?,
            val pveId: Long?,
            val name: String?,
            val description: String?,
            val status: VirtualMachineStatus?,
            val createdAt: Long?,
            val pveUserId: Long?,
            val systemUserName: String?,
            val hasSystemUserPassword: Boolean,
            val hasPublicKey: Boolean,
            val hasPrivateKey: Boolean,
        )

        val rs = Response(
            id = virtualMachine.id,
            pveId = virtualMachine.pveId,
            name = virtualMachine.name,
            description = virtualMachine.description,
            status = virtualMachine.status,
            createdAt = virtualMachine.createdAt,
            pveUserId = virtualMachine.pveUserId,
            systemUserName = virtualMachine.systemUserName,
            hasSystemUserPassword = virtualMachine.hasSystemUserPassword,
            hasPublicKey = virtualMachine.hasPublicKey,
            hasPrivateKey = virtualMachine.hasPrivateKey,
        )

        return builder.ok().data(rs).build()
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody request: VirtualMachineStatusUpdateRequest,
    ): ResponseEntity<Response> {
        val virtualMachine = pveManagementService.updateVirtualMachineStatus(id, request)

        data class Response(
            val id: Long?,
            val pveId: Long?,
            val name: String?,
            val description: String?,
            val status: VirtualMachineStatus?,
            val createdAt: Long?,
            val pveUserId: Long?,
            val systemUserName: String?,
            val hasSystemUserPassword: Boolean,
            val hasPublicKey: Boolean,
            val hasPrivateKey: Boolean,
        )

        val rs = Response(
            id = virtualMachine.id,
            pveId = virtualMachine.pveId,
            name = virtualMachine.name,
            description = virtualMachine.description,
            status = virtualMachine.status,
            createdAt = virtualMachine.createdAt,
            pveUserId = virtualMachine.pveUserId,
            systemUserName = virtualMachine.systemUserName,
            hasSystemUserPassword = virtualMachine.hasSystemUserPassword,
            hasPublicKey = virtualMachine.hasPublicKey,
            hasPrivateKey = virtualMachine.hasPrivateKey,
        )

        return builder.ok().data(rs).build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Response> {
        pveManagementService.deleteVirtualMachine(id)

        data class Response(
            val deleted: Boolean,
        )

        val rs = Response(
            deleted = true,
        )

        return builder.ok().data(rs).build()
    }
}
