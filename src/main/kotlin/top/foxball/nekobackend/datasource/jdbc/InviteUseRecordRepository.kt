package top.foxball.nekobackend.datasource.jdbc

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InviteUseRecordRepository : JpaRepository<InviteUseRecord, Long> {
    fun findByInviteCodeIdOrderByUsedAtDesc(inviteCodeId: Long, pageable: Pageable): Page<InviteUseRecord>
}
