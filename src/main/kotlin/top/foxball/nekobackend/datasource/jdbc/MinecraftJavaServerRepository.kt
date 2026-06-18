package top.foxball.nekobackend.datasource.jdbc

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MinecraftJavaServerRepository : JpaRepository<MinecraftJavaServer, Long> {
    fun findByUserId(userId: Long): List<MinecraftJavaServer>

    fun findByIdAndUserId(id: Long, userId: Long): MinecraftJavaServer?

    fun findByName(name: String): MinecraftJavaServer?

    fun findByUserIdAndNameContainingIgnoreCase(userId: Long, name: String): List<MinecraftJavaServer>

    fun findByVersion(version: String): List<MinecraftJavaServer>

    fun findByUserIdAndVersion(userId: Long, version: String): List<MinecraftJavaServer>

    fun findByServerType(serverType: MinecraftJavaServerType): List<MinecraftJavaServer>

    fun findByUserIdAndServerType(
        userId: Long,
        serverType: MinecraftJavaServerType,
    ): List<MinecraftJavaServer>

    fun findByStatus(status: MinecraftJavaServerStatus): List<MinecraftJavaServer>

    fun findByUserIdAndStatus(
        userId: Long,
        status: MinecraftJavaServerStatus,
    ): List<MinecraftJavaServer>

    fun findByAddressAndPort(address: String, port: Int): MinecraftJavaServer?

    fun findByUserIdAndAddressAndPort(
        userId: Long,
        address: String,
        port: Int,
    ): MinecraftJavaServer?

    fun findByVirtualMachineId(virtualMachineId: Long): List<MinecraftJavaServer>

    fun findByUserIdAndVirtualMachineId(
        userId: Long,
        virtualMachineId: Long,
    ): List<MinecraftJavaServer>

    fun existsByAddressAndPort(address: String, port: Int): Boolean

    fun existsByUserIdAndAddressAndPort(userId: Long, address: String, port: Int): Boolean
}
