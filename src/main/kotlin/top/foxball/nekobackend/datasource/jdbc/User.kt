package top.foxball.nekobackend.datasource.jdbc

import jakarta.persistence.*
import java.time.LocalDateTime

/** 用户账号实体，对应系统用户基础资料、封禁信息和权限关系。 */
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    /** 用户名，唯一且不能为空。 */
    @Column(name = "username", nullable = false, unique = true, length = 64)
    var username: String,

    /** 加密后的登录密码，不能为空。 */
    @Column(name = "password", nullable = false, length = 255)
    var password: String,

    /** 邮箱地址，唯一且不能为空。 */
    @Column(name = "email", nullable = false, unique = true, length = 128)
    var email: String,

    /** 用户昵称，不能为空。 */
    @Column(name = "nickname", nullable = false, length = 64)
    var nickname: String = "Neko",

    /** 注册时间，不能为空。 */
    @Column(name = "register_time", nullable = false)
    var registerTime: LocalDateTime? = null,

    /** 用户状态，ACTIVE 表示正常，BANNED 表示已封禁。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    var status: Status? = Status.ACTIVE,

    /** 封禁原因，未封禁时为空。 */
    @Column(name = "ban_reason", length = 255)
    var banReason: String? = null,

    /** 封禁时间，未封禁时为空。 */
    @Column(name = "banned_at")
    var bannedAt: LocalDateTime? = null,

    /** 执行封禁操作的用户 ID，未封禁或系统操作时为空。 */
    @Column(name = "banned_by_user_id")
    var bannedByUserId: Long? = null,

    /** 个性签名，可以为空。 */
    @Column(name = "signature", length = 255)
    var signature: String? = null,

    /** 头像 URL，可以为空，默认使用系统默认头像。 */
    @Column(name = "avatar", length = 255)
    var avatar: String? = "https://cdn.jsdelivr.net/gh/sakuranoki/cdn/img/avatar/default.png",

    /** 学号，可以为空。 */
    @Column(name = "student_id", length = 64)
    var studentId: String? = null,

    /** 年级，可以为空。 */
    @Column(name = "grade", length = 32)
    var grade: String? = null,

    /** 班级名称，可以为空。 */
    @Column(name = "class_name", length = 64)
    var className: String? = null,

    /** 专业名称，可以为空。 */
    @Column(name = "major", length = 128)
    var major: String? = null,

    /** 手机号，可以为空。 */
    @Column(name = "phone", length = 32)
    var phone: String? = null,

    /** QQ 号，可以为空。 */
    @Column(name = "qq_number", length = 32)
    var qqNumber: String? = null,

    /** 是否公开学号。 */
    @Column(name = "is_student_id")
    var isStudentId: Boolean = false,

    /** 用户分组，可以为空。 */
    @Column(name = "is_grouping", length = 64)
    var isGrouping: String? = null,

    /** 是否公开班级名称。 */
    @Column(name = "is_class_name")
    var isClassName: Boolean = false,

    /** 是否公开专业名称。 */
    @Column(name = "is_major")
    var isMajor: Boolean = false,

    /** 是否公开手机号。 */
    @Column(name = "is_phone")
    var isPhone: Boolean = false,

    /** 是否公开 QQ 号。 */
    @Column(name = "is_qq_number")
    var isQQNumber: Boolean = false,

    /** 其他联系方式列表，可以为空。 */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_contact_information",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
    )
    @Column(name = "contact_information", length = 128)
    var contactInformation: MutableList<String>? = null,

    /** 用户标签列表。 */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_tag",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id", referencedColumnName = "id")],
        uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "tag_id"])],
    )
    var tags: MutableSet<Tag> = mutableSetOf(),

    /** 角色列表。 */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")],
    )
    var roles: MutableSet<Role> = mutableSetOf(),

    /** 直接授予用户的权限列表。 */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_permission",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id", referencedColumnName = "id")],
    )
    var permissions: MutableSet<Permission> = mutableSetOf(),
)

/** 用户账号状态。 */
enum class Status {
    /** 正常可用。 */
    ACTIVE,

    /** 已被封禁。 */
    BANNED
}
