package top.foxball.nekobackend.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.nekobackend.datasource.jdbc.InviteCodeStatus
import top.foxball.nekobackend.security.AuthPrincipal
import top.foxball.nekobackend.service.CreateInviteCodeRequest
import top.foxball.nekobackend.service.InviteCodeService
import top.foxball.nekobackend.shared.Response
import top.foxball.nekobackend.shared.ResponseBuilder
import java.time.LocalDateTime

/**
 * 普通用户邀请码接口，负责当前用户创建自己的邀请码。
 */
@RestController
@RequestMapping("/api/invite-codes")
class InviteCodeController(
    private val inviteCodeService: InviteCodeService,
    private val builder: ResponseBuilder,
) {

    /**
     * 为当前登录用户创建邀请码，并返回本次生成的明文邀请码。
     */
    @PostMapping
    fun createMine(
        authentication: Authentication,
        @RequestBody request: CreateInviteCodeRequest,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val result = inviteCodeService.createForUser(
            createdByUserId = principal.userId,
            request = request,
        )
        val inviteCode = result.inviteCode

        data class Response(
            val id: Long?,
            val code: String?,
            val createdByUserId: Long?,
            val maxUses: Int,
            val usedCount: Int,
            val expiresAt: LocalDateTime?,
            val status: InviteCodeStatus,
            val bindEmail: String?,
            val remark: String?,
            val createdAt: LocalDateTime?,
            val updatedAt: LocalDateTime?,
        )

        val rs = Response(
            id = inviteCode.id,
            code = result.plainCode,
            createdByUserId = inviteCode.createdByUserId,
            maxUses = inviteCode.maxUses,
            usedCount = inviteCode.usedCount,
            expiresAt = inviteCode.expiresAt,
            status = inviteCode.status,
            bindEmail = inviteCode.bindEmail,
            remark = inviteCode.remark,
            createdAt = inviteCode.createdAt,
            updatedAt = inviteCode.updatedAt,
        )

        return builder.ok().data(rs).build()
    }
}
