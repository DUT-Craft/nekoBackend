package top.foxball.nekobackend.datasource.jdbc

import jakarta.persistence.*
import java.time.LocalDateTime

/** Minecraft Java 版服务端登记实体，保存服务端地址、版本、核心类型和运行状态等信息。 */
@Entity
@Table(
    name = "minecraft_java_server",
    indexes = [
        Index(name = "idx_minecraft_java_server_user_id", columnList = "user_id"),
        Index(name = "idx_minecraft_java_server_version", columnList = "version"),
        Index(name = "idx_minecraft_java_server_status", columnList = "status"),
        Index(name = "idx_minecraft_java_server_address_port", columnList = "address,port"),
    ],
)
class MinecraftJavaServer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null

    /** 登记该 Minecraft 服务端的系统用户 ID。 */
    @Column(name = "user_id")
    var userId: Long? = null

    /** 服务端名称，用于后台列表和用户侧展示。 */
    @Column(name = "name", nullable = false, length = 128)
    var name: String? = null

    /** 服务端说明。 */
    @Column(name = "description", length = 255)
    var description: String? = null

    /** Minecraft Java 版服务端版本，例如 1.20.1、1.21.1。 */
    @Column(name = "version", nullable = false, length = 64)
    var version: String? = null

    /** 服务端核心类型，例如原版、Paper、Spigot、Forge、Fabric。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "server_type", nullable = false, length = 32)
    var serverType: MinecraftJavaServerType? = MinecraftJavaServerType.VANILLA

    /** 服务端运行状态。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: MinecraftJavaServerStatus? = MinecraftJavaServerStatus.REGISTERED

    /** 服务端访问地址，可以是 IP 地址或域名。 */
    @Column(name = "address", nullable = false, length = 128)
    var address: String? = null

    /** 服务端访问端口，Java 版默认端口通常为 25565。 */
    @Column(name = "port", nullable = false)
    var port: Int? = 25565

    /** 服务端 MOTD 文本。 */
    @Column(name = "motd", length = 255)
    var motd: String? = null

    /** 最大在线人数。 */
    @Column(name = "max_players")
    var maxPlayers: Int? = null

    /** 是否开启正版验证。 */
    @Column(name = "online_mode")
    var onlineMode: Boolean? = true

    /** 是否开启白名单。 */
    @Column(name = "whitelist_enabled")
    var whitelistEnabled: Boolean? = false

    /** 服务端图标地址。 */
    @Column(name = "icon_url", length = 512)
    var iconUrl: String? = null

    /** 服务端所在虚拟机 ID，可以关联 PVE 虚拟机登记信息。 */
    @Column(name = "virtual_machine_id")
    var virtualMachineId: Long? = null

    /** 最后一次服务端状态检查时间。 */
    @Column(name = "last_checked_at")
    var lastCheckedAt: LocalDateTime? = null

    /** 登记时间。 */
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null

    /** 更新时间。 */
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null

    /** 备注信息。 */
    @Column(name = "remark", length = 255)
    var remark: String? = null
}

/** Minecraft Java 版服务端核心类型。 */
enum class MinecraftJavaServerType {
    /** Mojang 原版服务端。 */
    VANILLA,

    /** Paper 服务端。 */
    PAPER,

    /** Spigot 服务端。 */
    SPIGOT,

    /** Bukkit 服务端。 */
    BUKKIT,

    /** Forge 模组服务端。 */
    FORGE,

    /** Fabric 模组服务端。 */
    FABRIC,

    /** NeoForge 模组服务端。 */
    NEOFORGE,

    /** 其他服务端核心。 */
    OTHER,
}

/** Minecraft Java 版服务端登记状态。 */
enum class MinecraftJavaServerStatus {
    /** 已登记，尚未确认运行状态。 */
    REGISTERED,

    /** 正在运行。 */
    RUNNING,

    /** 已停止。 */
    STOPPED,

    /** 启动中。 */
    STARTING,

    /** 停止中。 */
    STOPPING,

    /** 无法连接。 */
    OFFLINE,

    /** 已禁用，不参与自动检查或展示。 */
    DISABLED,

    /** 状态未知。 */
    UNKNOWN,
}
