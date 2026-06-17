package top.foxball.nekobackend.datasource.jdbc

import jakarta.persistence.*

@Entity
@Table(name = "permission")
data class Permission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "code", nullable = false, unique = true, length = 128)
    var code: String? = null,

    @Column(name = "name", nullable = false, length = 64)
    var name: String? = null,

    @Column(name = "description", length = 255)
    var description: String? = null,
)
