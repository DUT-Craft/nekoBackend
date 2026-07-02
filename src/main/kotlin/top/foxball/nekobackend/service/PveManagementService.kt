package top.foxball.nekobackend.service

import org.springframework.stereotype.Service
import top.foxball.nekobackend.datasource.jdbc.VirtualMachineStatus

/**
 * PVE 用户创建请求。
 */
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

/**
 * PVE 用户更新请求。
 *
 * 空值字段表示不修改对应属性。
 */
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

/**
 * PVE 用户响应数据。
 *
 * 敏感字段只返回是否已配置，不返回明文内容。
 */
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

/**
 * 虚拟机创建请求。
 */
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

/**
 * 虚拟机更新请求。
 *
 * 空值字段表示不修改对应属性。
 */
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

/**
 * 虚拟机状态更新请求。
 */
data class VirtualMachineStatusUpdateRequest(
    val status: VirtualMachineStatus,
)

/**
 * 虚拟机响应数据。
 *
 * 系统密码和密钥只返回是否已配置，不返回明文内容。
 */
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

/**
 * PVE 管理服务，负责 PVE 用户和虚拟机记录的维护。
 */
@Service
interface PveManagementService {
    /**
     * 创建 PVE 用户记录。
     */
    fun createPveUser(request: PveUserCreateRequest): PveUserResponse

    /**
     * 查询 PVE 用户列表，可按用户、账号、邮箱、状态和地址过滤。
     */
    fun listPveUsers(
        userId: Long? = null,
        username: String? = null,
        email: String? = null,
        status: String? = null,
        address: String? = null,
    ): List<PveUserResponse>

    /**
     * 查询指定 PVE 用户详情。
     */
    fun getPveUser(id: Long): PveUserResponse

    /**
     * 更新指定 PVE 用户信息。
     */
    fun updatePveUser(id: Long, request: PveUserUpdateRequest): PveUserResponse

    /**
     * 删除指定 PVE 用户，并删除其关联虚拟机记录。
     */
    fun deletePveUser(id: Long)

    /**
     * 创建虚拟机记录。
     */
    fun createVirtualMachine(request: VirtualMachineCreateRequest): VirtualMachineResponse

    /**
     * 查询虚拟机列表，可按 PVE 用户、PVE 虚拟机 ID、名称和状态过滤。
     */
    fun listVirtualMachines(
        pveUserId: Long? = null,
        pveId: Long? = null,
        name: String? = null,
        status: VirtualMachineStatus? = null,
    ): List<VirtualMachineResponse>

    /**
     * 查询指定虚拟机详情。
     */
    fun getVirtualMachine(id: Long): VirtualMachineResponse

    /**
     * 更新指定虚拟机信息。
     */
    fun updateVirtualMachine(id: Long, request: VirtualMachineUpdateRequest): VirtualMachineResponse

    /**
     * 更新指定虚拟机状态。
     */
    fun updateVirtualMachineStatus(id: Long, request: VirtualMachineStatusUpdateRequest): VirtualMachineResponse

    /**
     * 删除指定虚拟机记录。
     */
    fun deleteVirtualMachine(id: Long)
}
