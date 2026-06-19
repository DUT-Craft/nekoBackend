package top.foxball.nekobackend.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.nekobackend.datasource.jdbc.InviteCode
import top.foxball.nekobackend.security.AuthPrincipal
import top.foxball.nekobackend.service.CreateInviteCodeRequest
import top.foxball.nekobackend.service.InviteCodeService
import top.foxball.nekobackend.shared.Response
import top.foxball.nekobackend.shared.ResponseBuilder

@RestController
@RequestMapping("/api/invite-codes")
class InviteCodeController(
    private val inviteCodeService: InviteCodeService,
    private val builder: ResponseBuilder,
) {

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

        return builder.ok().data(result.inviteCode.toResponse(result.plainCode)).build()
    }

    private fun InviteCode.toResponse(plainCode: String): InviteCodeResponse {
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
}
