package top.foxball.nekobackend.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import top.foxball.nekobackend.datasource.jdbc.User
import top.foxball.nekobackend.handlder.ParamErrorException
import top.foxball.nekobackend.handlder.UserNotFoundException
import top.foxball.nekobackend.security.AuthPrincipal
import top.foxball.nekobackend.service.UserService
import top.foxball.nekobackend.shared.Response
import top.foxball.nekobackend.shared.ResponseBuilder

@RestController
@RequestMapping("/api/")
class UserController(
    private val userService: UserService,
    private val builder: ResponseBuilder
) {
    @GetMapping("userByUsername")
    fun getUserByUsername(
        @RequestParam username: String
    ) : ResponseEntity<Response>{
        val user = userService.findByUsername(username)

        data class Response(
            val username: String,
            val nickname: String,
            val avatar: String,
            val signature: String
        )

        val rs = user?.let {
            Response(
                username = it.username,
                nickname = user.nickname,
                avatar = user.avatar!!,
                signature = user.signature!!
            )
        }

        return builder.ok().data(rs).build()
    }

    @PutMapping("user/profile")
    fun updateCurrentUserInfo(
        authentication: Authentication,
        @RequestBody request: UpdateUserInfoRequest,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val user = userService.findById(principal.userId) ?: throw UserNotFoundException()

        request.nickname?.trim()?.let {
            if (it.isBlank()) {
                throw ParamErrorException("昵称不能为空")
            }
            user.nickname = it
        }
        request.avatar?.let { user.avatar = normalizeNullableText(it) }
        request.signature?.let { user.signature = it.trim() }
        request.studentId?.let { user.studentId = normalizeNullableText(it) }
        request.grade?.let { user.grade = normalizeNullableText(it) }
        request.className?.let { user.className = normalizeNullableText(it) }
        request.major?.let { user.major = normalizeNullableText(it) }
        request.phone?.let { user.phone = normalizeNullableText(it) }
        request.contactInformation?.let { contacts ->
            user.contactInformation = contacts
                .mapNotNull { normalizeNullableText(it) }
                .toMutableList()
        }

        val savedUser = userService.save(user)
        return builder.ok().data(toUserInfoResponse(savedUser)).build()
    }

    private fun normalizeNullableText(value: String): String? {
        return value.trim().ifBlank { null }
    }

    private fun toUserInfoResponse(user: User): UserInfoResponse {
        return UserInfoResponse(
            id = user.id,
            username = user.username,
            email = user.email,
            nickname = user.nickname,
            avatar = user.avatar,
            signature = user.signature,
            studentId = user.studentId,
            grade = user.grade,
            className = user.className,
            major = user.major,
            phone = user.phone,
            contactInformation = user.contactInformation ?: emptyList(),
        )
    }
}

data class UpdateUserInfoRequest(
    val nickname: String? = null,
    val avatar: String? = null,
    val signature: String? = null,
    val studentId: String? = null,
    val grade: String? = null,
    val className: String? = null,
    val major: String? = null,
    val phone: String? = null,
    val contactInformation: List<String>? = null,
)

data class UserInfoResponse(
    val id: Long?,
    val username: String,
    val email: String,
    val nickname: String,
    val avatar: String?,
    val signature: String?,
    val studentId: String?,
    val grade: String?,
    val className: String?,
    val major: String?,
    val phone: String?,
    val contactInformation: List<String>,
)
