package top.foxball.nekobackend.service

import org.springframework.stereotype.Service
import top.foxball.nekobackend.datasource.jdbc.MinecraftJavaServerStatus
import top.foxball.nekobackend.datasource.jdbc.MinecraftJavaServerType
import java.time.LocalDateTime

/**
 * Minecraft Java 服务器创建请求。
 */
data class MinecraftJavaServerCreateRequest(
    val name: String = "",
    val description: String? = null,
    val version: String = "",
    val serverType: MinecraftJavaServerType? = MinecraftJavaServerType.VANILLA,
    val status: MinecraftJavaServerStatus? = MinecraftJavaServerStatus.REGISTERED,
    val address: String = "",
    val port: Int? = 25565,
    val motd: String? = null,
    val maxPlayers: Int? = null,
    val onlineMode: Boolean? = true,
    val whitelistEnabled: Boolean? = false,
    val iconUrl: String? = null,
    val virtualMachineId: Long? = null,
    val remark: String? = null,
)

/**
 * Minecraft Java 服务器更新请求。
 *
 * 空值字段表示不修改对应属性。
 */
data class MinecraftJavaServerUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val version: String? = null,
    val serverType: MinecraftJavaServerType? = null,
    val status: MinecraftJavaServerStatus? = null,
    val address: String? = null,
    val port: Int? = null,
    val motd: String? = null,
    val maxPlayers: Int? = null,
    val onlineMode: Boolean? = null,
    val whitelistEnabled: Boolean? = null,
    val iconUrl: String? = null,
    val virtualMachineId: Long? = null,
    val lastCheckedAt: LocalDateTime? = null,
    val remark: String? = null,
)

/**
 * Minecraft Java 服务器状态更新请求。
 */
data class MinecraftJavaServerStatusUpdateRequest(
    val status: MinecraftJavaServerStatus,
)

/**
 * Minecraft Java 服务器响应数据。
 */
data class MinecraftJavaServerResponse(
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

/**
 * Minecraft Java 服务器服务，负责当前用户服务器记录的创建、查询、更新和删除。
 */
@Service
interface MinecraftJavaServerService {
    /**
     * 为用户创建 Minecraft Java 服务器记录。
     */
    fun create(userId: Long, request: MinecraftJavaServerCreateRequest): MinecraftJavaServerResponse

    /**
     * 查询用户自己的服务器列表，可按版本、类型和状态过滤。
     */
    fun findMine(
        userId: Long,
        version: String? = null,
        serverType: MinecraftJavaServerType? = null,
        status: MinecraftJavaServerStatus? = null,
    ): List<MinecraftJavaServerResponse>

    /**
     * 查询用户自己的指定服务器详情。
     */
    fun findById(userId: Long, id: Long): MinecraftJavaServerResponse

    /**
     * 更新用户自己的指定服务器基础信息。
     */
    fun update(userId: Long, id: Long, request: MinecraftJavaServerUpdateRequest): MinecraftJavaServerResponse

    /**
     * 更新用户自己的指定服务器状态，并刷新检查时间。
     */
    fun updateStatus(
        userId: Long,
        id: Long,
        request: MinecraftJavaServerStatusUpdateRequest,
    ): MinecraftJavaServerResponse

    /**
     * 删除用户自己的指定服务器记录。
     */
    fun delete(userId: Long, id: Long)
}
