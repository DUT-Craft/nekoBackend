package top.foxball.nekobackend.datasource.jdbc

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TagRepository : JpaRepository<Tag, Long> {
    fun findByNormalizedName(normalizedName: String): Tag?
    fun findAllByOrderByNameAsc(): List<Tag>
    fun findByNormalizedNameContainingOrderByNameAsc(normalizedName: String): List<Tag>
}
