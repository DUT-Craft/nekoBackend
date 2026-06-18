package top.foxball.nekobackend.service

import org.springframework.stereotype.Service
import top.foxball.nekobackend.datasource.jdbc.MinecraftJavaServerStatus
import top.foxball.nekobackend.datasource.jdbc.MinecraftJavaServerType
import java.time.LocalDateTime

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

data class MinecraftJavaServerStatusUpdateRequest(
    val status: MinecraftJavaServerStatus,
)

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

@Service
interface MinecraftJavaServerService {
    fun create(userId: Long, request: MinecraftJavaServerCreateRequest): MinecraftJavaServerResponse

    fun findMine(
        userId: Long,
        version: String? = null,
        serverType: MinecraftJavaServerType? = null,
        status: MinecraftJavaServerStatus? = null,
    ): List<MinecraftJavaServerResponse>

    fun findById(userId: Long, id: Long): MinecraftJavaServerResponse

    fun update(userId: Long, id: Long, request: MinecraftJavaServerUpdateRequest): MinecraftJavaServerResponse

    fun updateStatus(
        userId: Long,
        id: Long,
        request: MinecraftJavaServerStatusUpdateRequest,
    ): MinecraftJavaServerResponse

    fun delete(userId: Long, id: Long)
}
