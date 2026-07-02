package top.foxball.nekobackend.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.nekobackend.datasource.jdbc.MinecraftJavaServer
import top.foxball.nekobackend.datasource.jdbc.MinecraftJavaServerRepository
import top.foxball.nekobackend.datasource.jdbc.MinecraftJavaServerStatus
import top.foxball.nekobackend.datasource.jdbc.MinecraftJavaServerType
import top.foxball.nekobackend.handlder.ParamErrorException
import top.foxball.nekobackend.handlder.ResourceNotFoundException
import top.foxball.nekobackend.service.*
import java.time.LocalDateTime

@Service
class MinecraftJavaServerServiceImpl(
    private val minecraftJavaServerRepository: MinecraftJavaServerRepository,
) : MinecraftJavaServerService {

    @Transactional
    override fun create(userId: Long, request: MinecraftJavaServerCreateRequest): MinecraftJavaServerResponse {
        val now = LocalDateTime.now()
        val server = MinecraftJavaServer().apply {
            this.userId = userId
            this.name = requireText(request.name, "服务端名称不能为空")
            this.description = normalizeNullableText(request.description)
            this.version = requireText(request.version, "Minecraft 版本不能为空")
            this.serverType = request.serverType ?: MinecraftJavaServerType.VANILLA
            this.status = request.status ?: MinecraftJavaServerStatus.REGISTERED
            this.address = requireText(request.address, "服务端地址不能为空")
            this.port = normalizePort(request.port)
            this.motd = normalizeNullableText(request.motd)
            this.maxPlayers = normalizePositiveOrNull(request.maxPlayers, "最大在线人数不能小于 0")
            this.onlineMode = request.onlineMode ?: true
            this.whitelistEnabled = request.whitelistEnabled ?: false
            this.iconUrl = normalizeNullableText(request.iconUrl)
            this.virtualMachineId = request.virtualMachineId
            this.createdAt = now
            this.updatedAt = now
            this.remark = normalizeNullableText(request.remark)
        }

        return minecraftJavaServerRepository.save(server).toResponse()
    }

    @Transactional(readOnly = true)
    override fun findMine(
        userId: Long,
        version: String?,
        serverType: MinecraftJavaServerType?,
        status: MinecraftJavaServerStatus?,
    ): List<MinecraftJavaServerResponse> {
        return minecraftJavaServerRepository.findByUserId(userId)
            .asSequence()
            .filter { version.isNullOrBlank() || it.version == version.trim() }
            .filter { serverType == null || it.serverType == serverType }
            .filter { status == null || it.status == status }
            .map { it.toResponse() }
            .toList()
    }

    @Transactional(readOnly = true)
    override fun findById(userId: Long, id: Long): MinecraftJavaServerResponse {
        return findOwnedServer(userId, id).toResponse()
    }

    @Transactional
    override fun update(
        userId: Long,
        id: Long,
        request: MinecraftJavaServerUpdateRequest,
    ): MinecraftJavaServerResponse {
        val server = findOwnedServer(userId, id)

        request.name?.let { server.name = requireText(it, "服务端名称不能为空") }
        request.description?.let { server.description = normalizeNullableText(it) }
        request.version?.let { server.version = requireText(it, "Minecraft 版本不能为空") }
        request.serverType?.let { server.serverType = it }
        request.status?.let { server.status = it }
        request.address?.let { server.address = requireText(it, "服务端地址不能为空") }
        request.port?.let { server.port = normalizePort(it) }
        request.motd?.let { server.motd = normalizeNullableText(it) }
        request.maxPlayers?.let { server.maxPlayers = normalizePositiveOrNull(it, "最大在线人数不能小于 0") }
        request.onlineMode?.let { server.onlineMode = it }
        request.whitelistEnabled?.let { server.whitelistEnabled = it }
        request.iconUrl?.let { server.iconUrl = normalizeNullableText(it) }
        request.virtualMachineId?.let { server.virtualMachineId = it }
        request.lastCheckedAt?.let { server.lastCheckedAt = it }
        request.remark?.let { server.remark = normalizeNullableText(it) }
        server.updatedAt = LocalDateTime.now()

        return minecraftJavaServerRepository.save(server).toResponse()
    }

    @Transactional
    override fun updateStatus(
        userId: Long,
        id: Long,
        request: MinecraftJavaServerStatusUpdateRequest,
    ): MinecraftJavaServerResponse {
        val server = findOwnedServer(userId, id)
        server.status = request.status
        server.lastCheckedAt = LocalDateTime.now()
        server.updatedAt = LocalDateTime.now()

        return minecraftJavaServerRepository.save(server).toResponse()
    }

    @Transactional
    override fun delete(userId: Long, id: Long) {
        val server = findOwnedServer(userId, id)
        minecraftJavaServerRepository.delete(server)
    }

    private fun findOwnedServer(userId: Long, id: Long): MinecraftJavaServer {
        val server = minecraftJavaServerRepository.findById(id).orElse(null)
            ?: throw ResourceNotFoundException("Minecraft 服务端不存在")

        if (server.userId != userId) {
            throw ResourceNotFoundException("Minecraft 服务端不存在")
        }

        return server
    }

    private fun requireText(value: String, message: String): String {
        return value.trim().ifBlank { throw ParamErrorException(message) }
    }

    private fun normalizeNullableText(value: String?): String? {
        return value?.trim()?.ifBlank { null }
    }

    private fun normalizePort(value: Int?): Int {
        val port = value ?: 25565
        if (port !in 1..65535) {
            throw ParamErrorException("服务端端口必须在 1 到 65535 之间")
        }
        return port
    }

    private fun normalizePositiveOrNull(value: Int?, message: String): Int? {
        if (value != null && value < 0) {
            throw ParamErrorException(message)
        }
        return value
    }

    private fun MinecraftJavaServer.toResponse(): MinecraftJavaServerResponse {
        return MinecraftJavaServerResponse(
            id = id,
            userId = userId,
            name = name,
            description = description,
            version = version,
            serverType = serverType,
            status = status,
            address = address,
            port = port,
            motd = motd,
            maxPlayers = maxPlayers,
            onlineMode = onlineMode,
            whitelistEnabled = whitelistEnabled,
            iconUrl = iconUrl,
            virtualMachineId = virtualMachineId,
            lastCheckedAt = lastCheckedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            remark = remark,
        )
    }
}
