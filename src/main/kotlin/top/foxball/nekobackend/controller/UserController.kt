package top.foxball.nekobackend.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
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
            val avatar: String?,
            val signature: String?,
            val studentId: String?,
            val grade: String?,
            val className: String?,
            val major: String?,
            val phone: String?,
            val qqNumber: String?,
            val isStudentId: Boolean,
            val isGrouping: String?,
            val isClassName: Boolean,
            val isMajor: Boolean,
            val isPhone: Boolean,
            val isQQNumber: Boolean,
            val contactInformation: List<String>,
        )

        val rs = user?.let {
            Response(
                username = it.username,
                nickname = it.nickname,
                avatar = it.avatar,
                signature = it.signature,
                studentId = it.studentId.takeIf { _ -> it.isStudentId },
                grade = it.grade,
                className = it.className.takeIf { _ -> it.isClassName },
                major = it.major.takeIf { _ -> it.isMajor },
                phone = it.phone.takeIf { _ -> it.isPhone },
                qqNumber = it.qqNumber.takeIf { _ -> it.isQQNumber },
                isStudentId = it.isStudentId,
                isGrouping = it.isGrouping,
                isClassName = it.isClassName,
                isMajor = it.isMajor,
                isPhone = it.isPhone,
                isQQNumber = it.isQQNumber,
                contactInformation = it.contactInformation ?: emptyList(),
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
        request.qqNumber?.let { user.qqNumber = normalizeNullableText(it) }
        request.isStudentId?.let { user.isStudentId = it }
        request.isGrouping?.let { user.isGrouping = normalizeNullableText(it) }
        request.isClassName?.let { user.isClassName = it }
        request.isMajor?.let { user.isMajor = it }
        request.isPhone?.let { user.isPhone = it }
        request.isQQNumber?.let { user.isQQNumber = it }
        request.contactInformation?.let { contacts ->
            user.contactInformation = contacts
                .mapNotNull { normalizeNullableText(it) }
                .toMutableList()
        }

        val savedUser = userService.save(user)

        data class Response(
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
            val qqNumber: String?,
            val isStudentId: Boolean,
            val isGrouping: String?,
            val isClassName: Boolean,
            val isMajor: Boolean,
            val isPhone: Boolean,
            val isQQNumber: Boolean,
            val contactInformation: List<String>,
        )

        val rs = Response(
            id = savedUser.id,
            username = savedUser.username,
            email = savedUser.email,
            nickname = savedUser.nickname,
            avatar = savedUser.avatar,
            signature = savedUser.signature,
            studentId = savedUser.studentId,
            grade = savedUser.grade,
            className = savedUser.className,
            major = savedUser.major,
            phone = savedUser.phone,
            qqNumber = savedUser.qqNumber,
            isStudentId = savedUser.isStudentId,
            isGrouping = savedUser.isGrouping,
            isClassName = savedUser.isClassName,
            isMajor = savedUser.isMajor,
            isPhone = savedUser.isPhone,
            isQQNumber = savedUser.isQQNumber,
            contactInformation = savedUser.contactInformation ?: emptyList(),
        )

        return builder.ok().data(rs).build()
    }

    private fun normalizeNullableText(value: String): String? {
        return value.trim().ifBlank { null }
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
    val qqNumber: String? = null,
    val isStudentId: Boolean? = null,
    val isGrouping: String? = null,
    val isClassName: Boolean? = null,
    val isMajor: Boolean? = null,
    val isPhone: Boolean? = null,
    val isQQNumber: Boolean? = null,
    val contactInformation: List<String>? = null,
)
