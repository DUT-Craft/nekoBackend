package top.foxball.nekobackend.service

import org.springframework.stereotype.Service
import top.foxball.nekobackend.datasource.jdbc.VirtualMachineStatus

data class PveUserCreateRequest(
    val userId: Long? = null,
    val username: String = "",
    val password: String? = null,
    val address: String = "",
    val port: String? = "8006",
    val pam: String? = null,
    val email: String? = null,
    val status: String? = "ACTIVE",
    val apiToken: String? = null,
)

data class PveUserUpdateRequest(
    val userId: Long? = null,
    val username: String? = null,
    val password: String? = null,
    val address: String? = null,
    val port: String? = null,
    val pam: String? = null,
    val email: String? = null,
    val status: String? = null,
    val apiToken: String? = null,
)

data class PveUserResponse(
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

data class VirtualMachineCreateRequest(
    val pveId: Long? = null,
    val name: String = "",
    val description: String? = null,
    val status: VirtualMachineStatus? = VirtualMachineStatus.UNKNOWN,
    val pveUserId: Long? = null,
    val systemUserName: String? = null,
    val systemUserPassword: String? = null,
    val publicKey: String? = null,
    val privateKey: String? = null,
)

data class VirtualMachineUpdateRequest(
    val pveId: Long? = null,
    val name: String? = null,
    val description: String? = null,
    val status: VirtualMachineStatus? = null,
    val pveUserId: Long? = null,
    val systemUserName: String? = null,
    val systemUserPassword: String? = null,
    val publicKey: String? = null,
    val privateKey: String? = null,
)

data class VirtualMachineStatusUpdateRequest(
    val status: VirtualMachineStatus,
)

data class VirtualMachineResponse(
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

@Service
interface PveManagementService {
    fun createPveUser(request: PveUserCreateRequest): PveUserResponse

    fun listPveUsers(
        userId: Long? = null,
        username: String? = null,
        email: String? = null,
        status: String? = null,
        address: String? = null,
    ): List<PveUserResponse>

    fun getPveUser(id: Long): PveUserResponse

    fun updatePveUser(id: Long, request: PveUserUpdateRequest): PveUserResponse

    fun deletePveUser(id: Long)

    fun createVirtualMachine(request: VirtualMachineCreateRequest): VirtualMachineResponse

    fun listVirtualMachines(
        pveUserId: Long? = null,
        pveId: Long? = null,
        name: String? = null,
        status: VirtualMachineStatus? = null,
    ): List<VirtualMachineResponse>

    fun getVirtualMachine(id: Long): VirtualMachineResponse

    fun updateVirtualMachine(id: Long, request: VirtualMachineUpdateRequest): VirtualMachineResponse

    fun updateVirtualMachineStatus(id: Long, request: VirtualMachineStatusUpdateRequest): VirtualMachineResponse

    fun deleteVirtualMachine(id: Long)
}
