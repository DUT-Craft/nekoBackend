package top.foxball.nekobackend.controller

import org.springframework.data.domain.Page
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
import top.foxball.nekobackend.datasource.jdbc.InviteCode
import top.foxball.nekobackend.datasource.jdbc.InviteCodeStatus
import top.foxball.nekobackend.datasource.jdbc.InviteUseRecord
import top.foxball.nekobackend.security.AuthPrincipal
import top.foxball.nekobackend.security.permission.RequireRole
import top.foxball.nekobackend.service.CreateInviteCodeRequest
import top.foxball.nekobackend.service.InviteCodeService
import top.foxball.nekobackend.shared.Response
import top.foxball.nekobackend.shared.ResponseBuilder
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/admin/invite-codes")
@RequireRole("ADMIN")
class AdminInviteCodeController(
    private val inviteCodeService: InviteCodeService,
    private val builder: ResponseBuilder,
) {

    @PostMapping
    fun create(
        authentication: Authentication,
        @RequestBody request: CreateInviteCodeRequest,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val inviteCode = inviteCodeService.create(
            createdByUserId = principal.userId,
            request = request,
        )

        return builder.ok().data(inviteCode.inviteCode.toResponse(plainCode = inviteCode.plainCode)).build()
    }

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

        return builder.ok().data(inviteCodes.toPageResponse { it.toResponse() }).build()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Response> {
        return builder.ok().data(inviteCodeService.findById(id).toResponse()).build()
    }

    @PutMapping("/{id}/disable")
    fun disable(@PathVariable id: Long): ResponseEntity<Response> {
        return builder.ok().data(inviteCodeService.disable(id).toResponse()).build()
    }

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

        return builder.ok().data(records.toPageResponse { it.toResponse() }).build()
    }

    private fun InviteCode.toResponse(plainCode: String? = null): InviteCodeResponse {
        return InviteCodeResponse(
            id = id,
            code = plainCode,
            createdByUserId = createdByUserId,
            maxUses = maxUses,
            usedCount = usedCount,
            expiresAt = expiresAt,
            status = status,
            bindEmail = bindEmail,
            remark = remark,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun InviteUseRecord.toResponse(): InviteUseRecordResponse {
        return InviteUseRecordResponse(
            id = id,
            inviteCodeId = inviteCodeId,
            inviteCodeHash = inviteCodeHash,
            registeredUserId = registeredUserId,
            username = username,
            email = email,
            userAgent = userAgent,
            usedAt = usedAt,
        )
    }

    private fun <T : Any, R> Page<T>.toPageResponse(mapper: (T) -> R): PageResponse<R> {
        return PageResponse(
            content = content.map(mapper),
            page = number,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
        )
    }
}

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

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
