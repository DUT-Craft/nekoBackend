package top.foxball.nekobackend.datasource.jdbc

import jakarta.persistence.*

/** PVE 平台用户实体，保存业务用户在 PVE 平台侧的账号和访问凭据。 */
@Entity
@Table(
    name = "pve_user",
    indexes = [
        Index(name = "idx_pve_user_user_id", columnList = "user_id"),
        Index(name = "idx_pve_user_username", columnList = "username"),
    ],
)
class PveUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null

    /** 系统用户 ID，用于关联本系统用户。 */
    @Column(name = "user_id")
    var userId: Long? = null

    /** PVE 平台登录用户名。 */
    @Column(name = "username", length = 64)
    var username: String? = null

    /** PVE 平台登录密码或加密后的密码。 */
    @Column(name = "password", length = 255)
    var password: String? = null

    /** PVE 服务器访问地址，可以是 IP 地址或域名。 */
    @Column(name = "address", length = 128)
    var address: String? = null

    /** PVE 服务端口，默认通常为 8006。 */
    @Column(name = "port", length = 32)
    var port: String? = null

    /** PVE PAM 认证域或认证用户名后缀配置。 */
    @Column(name = "pam", length = 128)
    var pam: String? = null

    /** PVE 账号绑定邮箱。 */
    @Column(name = "email", length = 128,nullable = true)
    var email: String? = null

    /** 创建时间戳。 */
    @Column(name = "created_at")
    var createdAt: Long? = null

    /** 更新时间戳。 */
    @Column(name = "updated_at")
    var updatedAt: Long? = null

    /** PVE 账号状态，例如 ACTIVE、DISABLED。 */
    @Column(name = "status", length = 32)
    var status: String? = null

    /** PVE API 访问令牌。 */
    @Column(name = "api_token", length = 512)
    var apiToken: String? = null
}

/** 虚拟机实体，保存 PVE 节点中的虚拟机基础信息和远程访问凭据。 */
@Entity
@Table(
    name = "virtual_machines",
    indexes = [
        Index(name = "idx_virtual_machines_pve_id", columnList = "pve_id"),
        Index(name = "idx_virtual_machines_pve_user_id", columnList = "pve_user_id"),
    ],
)
class VirtualMachines {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null

    /** PVE 平台侧虚拟机 ID。 */
    @Column(name = "pve_id")
    var pveId: Long? = null

    /** 虚拟机名称。 */
    @Column(name = "name", length = 128)
    var name: String? = null

    /** 虚拟机说明。 */
    @Column(name = "description", length = 255)
    var description: String? = null

    /** 虚拟机运行状态，例如 RUNNING、STOPPED。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32)
    var status: VirtualMachineStatus? = VirtualMachineStatus.UNKNOWN

    /** 创建时间戳。 */
    @Column(name = "created_at")
    var createdAt: Long? = null

    /** 关联的 PVE 用户实体 ID。 */
    @Column(name = "pve_user_id")
    var pveUserId: Long? = null

    /** 虚拟机系统登录用户名。 */
    @Column(name = "system_user_name", length = 64)
    var systemUserName: String? = null

    /** 虚拟机系统登录密码或加密后的密码。 */
    @Column(name = "system_user_password", length = 255)
    var systemUserPassword: String? = null

    /** 虚拟机 SSH 公钥或私钥内容。 */
    @Lob
    @Column(name = "ssh_key")
    var publicKey: String? = null

    /** 虚拟机 SSH 密钥内容。 */
    @Lob
    @Column(name = "ssh_key_private")
    var privateKey: String? = null
}

/** 虚拟机运行状态。 */
enum class VirtualMachineStatus {
    /** 运行中。 */
    RUNNING,

    /** 已停止。 */
    STOPPED,

    /** 暂停中。 */
    PAUSED,

    /** 挂起中。 */
    SUSPENDED,

    /** 启动中。 */
    STARTING,

    /** 停止中。 */
    STOPPING,

    /** 状态未知或暂未同步。 */
    UNKNOWN,
}
