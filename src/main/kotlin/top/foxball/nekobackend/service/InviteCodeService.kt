package top.foxball.nekobackend.service

import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import top.foxball.nekobackend.datasource.jdbc.InviteCode
import top.foxball.nekobackend.datasource.jdbc.InviteCodeStatus
import top.foxball.nekobackend.datasource.jdbc.InviteUseRecord
import top.foxball.nekobackend.datasource.jdbc.User
import java.time.LocalDateTime

/**
 * 邀请码创建请求。
 *
 * 未指定 [code] 时由系统生成邀请码；当前实现要求邀请码绑定邮箱。
 */
data class CreateInviteCodeRequest(
    val code: String? = null,
    val maxUses: Int = 1,
    val expiresAt: LocalDateTime? = null,
    val bindEmail: String? = null,
    val remark: String? = null,
)

/**
 * 邀请码创建结果。
 *
 * [plainCode] 只在创建时返回，持久化时保存的是哈希值。
 */
data class CreateInviteCodeResult(
    val inviteCode: InviteCode,
    val plainCode: String,
)

/**
 * 邀请码服务，负责邀请码创建、查询、禁用、注册校验和使用记录维护。
 */
@Service
interface InviteCodeService {
    /**
     * 创建邀请码。
     */
    fun create(
        createdByUserId: Long,
        request: CreateInviteCodeRequest,
    ): CreateInviteCodeResult

    /**
     * 为普通用户创建个人邀请码。
     *
     * 个人邀请码强制单次使用，并受用户可创建数量上限限制。
     */
    fun createForUser(
        createdByUserId: Long,
        request: CreateInviteCodeRequest,
    ): CreateInviteCodeResult

    /**
     * 分页查询邀请码，可按状态过滤。
     */
    fun list(
        page: Int,
        size: Int,
        status: InviteCodeStatus?,
    ): Page<InviteCode>

    /**
     * 根据主键查询邀请码。
     */
    fun findById(id: Long): InviteCode

    /**
     * 禁用指定邀请码。
     */
    fun disable(id: Long): InviteCode

    /**
     * 分页查询指定邀请码的使用记录。
     */
    fun listRecords(
        inviteCodeId: Long,
        page: Int,
        size: Int,
    ): Page<InviteUseRecord>

    /**
     * 校验注册用邀请码是否可用。
     *
     * 校验失败会记录同邮箱和 User-Agent 的失败次数，防止短时间内重复尝试。
     */
    fun validateForRegister(inviteCode: String, email: String, userAgent: String): InviteCode

    /**
     * 注册成功后消费邀请码并写入使用记录。
     */
    fun consumeForRegister(
        inviteCode: InviteCode,
        user: User,
        email: String,
        userAgent: String,
    )
}
