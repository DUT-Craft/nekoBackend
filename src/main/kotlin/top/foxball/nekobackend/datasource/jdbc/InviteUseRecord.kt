package top.foxball.nekobackend.datasource.jdbc

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "invite_use_records",
    indexes = [
        Index(name = "idx_invite_use_records_invite_code_id", columnList = "invite_code_id"),
        Index(name = "idx_invite_use_records_registered_user_id", columnList = "registered_user_id"),
    ],
)
class InviteUseRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "invite_code_id", nullable = false)
    var inviteCodeId: Long,

    @Column(name = "invite_code_hash", nullable = false, length = 64)
    var inviteCodeHash: String,

    @Column(name = "registered_user_id", nullable = false)
    var registeredUserId: Long,

    @Column(name = "username", nullable = false, length = 64)
    var username: String,

    @Column(name = "email", nullable = false, length = 128)
    var email: String,

    @Column(name = "user_agent", nullable = false, length = 512)
    var userAgent: String,

    @Column(name = "used_at", nullable = false)
    var usedAt: LocalDateTime? = null,
) {
    @PrePersist
    fun prePersist() {
        usedAt = usedAt ?: LocalDateTime.now()
    }
}
