package top.foxball.nekobackend.datasource.jdbc

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "invite_codes",
    indexes = [
        Index(name = "idx_invite_codes_code_hash", columnList = "code_hash", unique = true),
    ],
)
class InviteCode(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "code_hash", nullable = false, unique = true, length = 64)
    var codeHash: String,

    @Column(name = "created_by_user_id")
    var createdByUserId: Long? = null,

    @Column(name = "max_uses", nullable = false)
    var maxUses: Int = 1,

    @Column(name = "used_count", nullable = false)
    var usedCount: Int = 0,

    @Column(name = "expires_at")
    var expiresAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    var status: InviteCodeStatus = InviteCodeStatus.ACTIVE,

    @Column(name = "bind_email", length = 128)
    var bindEmail: String? = null,

    /** 保留旧字段但第三阶段不再使用域名绑定。 */
    @Column(name = "bind_email_domain", length = 128)
    var bindEmailDomain: String? = null,

    @Column(name = "remark", length = 255)
    var remark: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime? = null,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null,
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = createdAt ?: now
        updatedAt = updatedAt ?: now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

enum class InviteCodeStatus {
    ACTIVE,
    DISABLED,
    EXPIRED,
}
