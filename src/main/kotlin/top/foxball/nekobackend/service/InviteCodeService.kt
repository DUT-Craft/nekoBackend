package top.foxball.nekobackend.service

import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import top.foxball.nekobackend.datasource.jdbc.InviteCode
import top.foxball.nekobackend.datasource.jdbc.InviteCodeStatus
import top.foxball.nekobackend.datasource.jdbc.InviteUseRecord
import top.foxball.nekobackend.datasource.jdbc.User
import java.time.LocalDateTime

data class CreateInviteCodeRequest(
    val code: String? = null,
    val maxUses: Int = 1,
    val expiresAt: LocalDateTime? = null,
    val bindEmail: String? = null,
    val remark: String? = null,
)

data class CreateInviteCodeResult(
    val inviteCode: InviteCode,
    val plainCode: String,
)

@Service
interface InviteCodeService {
    fun create(
        createdByUserId: Long,
        request: CreateInviteCodeRequest,
    ): CreateInviteCodeResult

    fun createForUser(
        createdByUserId: Long,
        request: CreateInviteCodeRequest,
    ): CreateInviteCodeResult

    fun list(
        page: Int,
        size: Int,
        status: InviteCodeStatus?,
    ): Page<InviteCode>

    fun findById(id: Long): InviteCode

    fun disable(id: Long): InviteCode

    fun listRecords(
        inviteCodeId: Long,
        page: Int,
        size: Int,
    ): Page<InviteUseRecord>

    fun validateForRegister(inviteCode: String, email: String, userAgent: String): InviteCode

    fun consumeForRegister(
        inviteCode: InviteCode,
        user: User,
        email: String,
        userAgent: String,
    )
}
