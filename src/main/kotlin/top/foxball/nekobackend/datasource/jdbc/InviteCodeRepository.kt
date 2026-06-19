package top.foxball.nekobackend.datasource.jdbc

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface InviteCodeRepository : JpaRepository<InviteCode, Long> {
    fun findByCodeHash(codeHash: String): InviteCode?

    fun findByStatus(status: InviteCodeStatus, pageable: Pageable): Page<InviteCode>

    fun countByCreatedByUserId(createdByUserId: Long): Long

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select inviteCode from InviteCode inviteCode where inviteCode.codeHash = :codeHash")
    fun findByCodeHashForUpdate(@Param("codeHash") codeHash: String): InviteCode?
}
