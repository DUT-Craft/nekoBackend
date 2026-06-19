package top.foxball.nekobackend.datasource.jdbc

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/** 可复用的用户标签。 */
@Entity
@Table(name = "tag")
class Tag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    /** 标签展示名称。 */
    @Column(name = "name", nullable = false, length = 32)
    var name: String,

    /** 标签规范化名称，用于全局唯一复用。 */
    @Column(name = "normalized_name", nullable = false, unique = true, length = 32)
    var normalizedName: String,

    /** 创建该标签的用户 ID。 */
    @Column(name = "created_by_user_id")
    var createdByUserId: Long? = null,

    /** 标签创建时间。 */
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime? = null,
)
