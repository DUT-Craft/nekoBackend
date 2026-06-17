package top.foxball.nekobackend.datasource.jdbc

import jakarta.persistence.*

@Entity
@Table(name = "role")
data class Role(
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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permission",
        joinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id", referencedColumnName = "id")],
    )
    var permissions: MutableSet<Permission> = mutableSetOf(),
)
