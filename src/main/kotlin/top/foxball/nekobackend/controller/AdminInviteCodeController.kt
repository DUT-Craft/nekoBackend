package top.foxball.nekobackend.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import top.foxball.nekobackend.datasource.jdbc.InviteCodeStatus
import top.foxball.nekobackend.security.AuthPrincipal
import top.foxball.nekobackend.security.permission.RequireRole
import top.foxball.nekobackend.service.CreateInviteCodeRequest
import top.foxball.nekobackend.service.InviteCodeService
import top.foxball.nekobackend.shared.Response
import top.foxball.nekobackend.shared.ResponseBuilder
import java.time.LocalDateTime

/**
 * 管理端邀请码接口，提供邀请码创建、查询、禁用和使用记录查询能力。
 */
@RestController
@RequestMapping("/api/admin/invite-codes")
@RequireRole("ADMIN")
class AdminInviteCodeController(
    private val inviteCodeService: InviteCodeService,
    private val builder: ResponseBuilder,
) {

    /**
     * 管理员创建邀请码，并返回本次生成的明文邀请码。
     */
    @PostMapping
    fun create(
        authentication: Authentication,
        @RequestBody request: CreateInviteCodeRequest,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val result = inviteCodeService.create(
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

    /**
     * 分页查询邀请码列表，可按邀请码状态过滤。
     */
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: InviteCodeStatus?,
    ): ResponseEntity<Response> {
        val inviteCodes = inviteCodeService.list(
            page = page,
            size = size,
            status = status,
        )

        data class InviteCodeResponse(
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

        data class Response(
            val content: List<InviteCodeResponse>,
            val page: Int,
            val size: Int,
            val totalElements: Long,
            val totalPages: Int,
        )

        val rs = Response(
            content = inviteCodes.content.map {
                InviteCodeResponse(
                    id = it.id,
                    code = null,
                    createdByUserId = it.createdByUserId,
                    maxUses = it.maxUses,
                    usedCount = it.usedCount,
                    expiresAt = it.expiresAt,
                    status = it.status,
                    bindEmail = it.bindEmail,
                    remark = it.remark,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            page = inviteCodes.number,
            size = inviteCodes.size,
            totalElements = inviteCodes.totalElements,
            totalPages = inviteCodes.totalPages,
        )

        return builder.ok().data(rs).build()
    }

    /**
     * 根据邀请码 ID 查询邀请码详情。
     */
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Response> {
        val inviteCode = inviteCodeService.findById(id)

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
            code = null,
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

    /**
     * 禁用指定邀请码。
     */
    @PutMapping("/{id}/disable")
    fun disable(@PathVariable id: Long): ResponseEntity<Response> {
        val inviteCode = inviteCodeService.disable(id)

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
            code = null,
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

    /**
     * 分页查询指定邀请码的使用记录。
     */
    @GetMapping("/{id}/records")
    fun records(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<Response> {
        val records = inviteCodeService.listRecords(
            inviteCodeId = id,
            page = page,
            size = size,
        )

        data class InviteUseRecordResponse(
            val id: Long?,
            val inviteCodeId: Long,
            val inviteCodeHash: String,
            val registeredUserId: Long,
            val username: String,
            val email: String,
            val userAgent: String,
            val usedAt: LocalDateTime?,
        )

        data class Response(
            val content: List<InviteUseRecordResponse>,
            val page: Int,
            val size: Int,
            val totalElements: Long,
            val totalPages: Int,
        )

        val rs = Response(
            content = records.content.map {
                InviteUseRecordResponse(
                    id = it.id,
                    inviteCodeId = it.inviteCodeId,
                    inviteCodeHash = it.inviteCodeHash,
                    registeredUserId = it.registeredUserId,
                    username = it.username,
                    email = it.email,
                    userAgent = it.userAgent,
                    usedAt = it.usedAt,
                )
            },
            page = records.number,
            size = records.size,
            totalElements = records.totalElements,
            totalPages = records.totalPages,
        )

        return builder.ok().data(rs).build()
    }
}
