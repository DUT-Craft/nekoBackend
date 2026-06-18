package top.foxball.nekobackend.datasource.jdbc

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PveUserRepository : JpaRepository<PveUser, Long> {
    fun findByUserId(userId: Long): List<PveUser>

    fun findFirstByUserId(userId: Long): PveUser?

    fun findByUsername(username: String): PveUser?

    fun findByEmail(email: String): PveUser?

    fun findByStatus(status: String): List<PveUser>

    fun findByAddress(address: String): List<PveUser>

    fun findByAddressAndPort(address: String, port: String): List<PveUser>

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun deleteByUserId(userId: Long): Long
}
