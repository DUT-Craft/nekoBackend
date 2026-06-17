package top.foxball.nekobackend.datasource.jdbc

import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "user")
@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    /** 用户名，唯一约束，不允许为空 */
    @Column(name = "username", nullable = false, unique = true)
    var username: String? = null,

    /** 密码，不允许为空 */
    @Column(name = "password", nullable = false)
    var password: String? = null,

    /** 邮箱地址，唯一约束，不允许为空 */
    @Column(name = "email", nullable = false, unique = true)
    var email: String,

    /** 昵称，不允许为空 */
    @Column(name = "nickname", nullable = false)
    var nickname: String? = null,

    /** 注册时间，不允许为空 */
    @Column(name = "register_time", nullable = false)
    var registerTime: LocalDateTime? = null,

    /** 用户状态（ACTIVE-活跃，BANNED-禁用） */
    @Column(name = "status", nullable = false)
    var status: Status? = Status.ACTIVE,

    @Column(name = "ban_reason", length = 255)
    var banReason: String? = null,

    @Column(name = "banned_at")
    var bannedAt: LocalDateTime? = null,

    @Column(name = "banned_by_user_id")
    var bannedByUserId: Long? = null,

    /** 个性签名，可为空 */
    @Column(name = "signature", nullable = true)
    var signature: String? = null,

    /** 头像 URL，可为空，默认使用系统默认头像 */
    @Column(name = "avatar", nullable = true)
    var avatar: String? = "https://cdn.jsdelivr.net/gh/sakuranoki/cdn/img/avatar/default.png",

    /** 角色列表 */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")],
    )
    var roles: MutableSet<Role> = mutableSetOf(),

    /** 权限列表 */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_permission",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id", referencedColumnName = "id")],
    )
    var permissions: MutableSet<Permission> = mutableSetOf(),

    )

enum class Status {
    ACTIVE, BANNED
}
