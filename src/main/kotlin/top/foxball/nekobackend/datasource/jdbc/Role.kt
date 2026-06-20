package top.foxball.nekobackend.datasource.jdbc

import jakarta.persistence.*

@Entity
@Table(name = "role")
 class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "code", nullable = false, unique = true, length = 64)
    var code: String? = null,

    @Column(name = "name", nullable = false, length = 64)
    var name: String? = null,

    @Column(name = "description", length = 255)
    var description: String? = null,
)
