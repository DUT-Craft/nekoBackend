package top.foxball.nekobackend.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import top.foxball.nekobackend.handlder.ParamErrorException
import top.foxball.nekobackend.handlder.UserAlreadyExistsException
import top.foxball.nekobackend.handlder.UserNotFoundException
import top.foxball.nekobackend.security.AuthPrincipal
import top.foxball.nekobackend.service.*
import top.foxball.nekobackend.shared.Response
import top.foxball.nekobackend.shared.ResponseBuilder
import java.util.*

/**
 * 用户资料相关接口，负责公开用户资料查询、当前用户邮箱和个人资料维护。
 */
@RestController
@RequestMapping("/api/")
class UserController(
    private val userService: UserService,
    private val emailVerificationService: EmailVerificationService,
    private val fileStorageService: FileStorageService,
    private val builder: ResponseBuilder
) {
    /**
     * 根据用户名查询用户公开资料。
     */
    @GetMapping("userByUsername")
    fun getUserByUsername(
        @RequestParam username: String
    ): ResponseEntity<Response> {
        val user = userService.findByUsername(username)

        data class Response(
            val username: String,
            val nickname: String,
            val identity: String?,
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
            val tags: List<TagResponse>,
        )

        val rs = user?.let {
            Response(
                username = it.username,
                nickname = it.nickname,
                identity = it.identity,
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
                tags = it.tags.toTagResponses(),
            )
        }

        return builder.ok().data(rs).build()
    }

    /**
     * 向当前登录用户的新邮箱发送换绑验证码。
     */
    @PostMapping("user/email-code")
    fun sendChangeEmailCode(
        authentication: Authentication,
        @RequestBody request: SendChangeEmailCodeRequest,
        @RequestHeader(HttpHeaders.USER_AGENT, required = true) userAgent: String,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val user = userService.findById(principal.userId) ?: throw UserNotFoundException()
        val email = request.email.trim()
        if (email.isBlank()) {
            throw ParamErrorException("邮箱不能为空")
        }
        if (email == user.email) {
            throw ParamErrorException("新邮箱不能与当前邮箱相同")
        }
        val usedUser = userService.findByEmail(email)
        if (usedUser != null && usedUser.id != user.id) {
            throw UserAlreadyExistsException("邮箱已存在")
        }

        val result = emailVerificationService.sendCode(
            username = user.username,
            email = email,
            purpose = EmailVerificationPurpose.CHANGE_EMAIL,
            userAgent = userAgent,
        )

        data class Response(
            val sent: Boolean,
            val expiresInSeconds: Long,
        )

        val rs = Response(
            sent = result.sent,
            expiresInSeconds = result.expiresInSeconds,
        )

        return builder.ok().data(rs).build()
    }

    /**
     * 校验邮箱验证码并修改当前登录用户邮箱。
     */
    @PutMapping("user/email")
    fun changeCurrentUserEmail(
        authentication: Authentication,
        @RequestBody request: ChangeEmailRequest,
        @RequestHeader(HttpHeaders.USER_AGENT, required = true) userAgent: String,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val user = userService.findById(principal.userId) ?: throw UserNotFoundException()
        val email = request.email.trim()
        if (email.isBlank() || request.verificationCode.isBlank()) {
            throw ParamErrorException("邮箱和验证码不能为空")
        }
        if (email == user.email) {
            throw ParamErrorException("新邮箱不能与当前邮箱相同")
        }
        val usedUser = userService.findByEmail(email)
        if (usedUser != null && usedUser.id != user.id) {
            throw UserAlreadyExistsException("邮箱已存在")
        }

        emailVerificationService.verifyCode(
            username = user.username,
            email = email,
            code = request.verificationCode,
            purpose = EmailVerificationPurpose.CHANGE_EMAIL,
            userAgent = userAgent,
        )

        user.email = email
        val savedUser = userService.save(user)

        data class Response(
            val changed: Boolean,
            val email: String,
        )

        val rs = Response(
            changed = true,
            email = savedUser.email,
        )

        return builder.ok().data(rs).build()
    }

    /**
     * 更新当前登录用户的个人资料和资料可见性配置。
     */
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
        request.identity?.let { user.identity = normalizeNullableText(it) }
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
            val identity: String?,
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
            val tags: List<TagResponse>,
        )

        val rs = Response(
            id = savedUser.id,
            username = savedUser.username,
            email = savedUser.email,
            nickname = savedUser.nickname,
            identity = savedUser.identity,
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
            tags = savedUser.tags.toTagResponses(),
        )

        return builder.ok().data(rs).build()
    }

    /**
     * 上传当前登录用户头像，并把头像地址更新为文件下载地址。
     */
    @PostMapping("user/avatar")
    fun uploadCurrentUserAvatar(
        authentication: Authentication,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        val user = userService.findById(principal.userId) ?: throw UserNotFoundException()
        validateAvatarFile(file)

        val uploadedFile = fileStorageService.upload(principal.userId, file)
        user.avatar = uploadedFile.downloadUrl
        val savedUser = userService.save(user)

        data class Response(
            val avatar: String?,
            val file: FileUploadResponse,
        )

        return builder.ok()
            .data(
                Response(
                    avatar = savedUser.avatar,
                    file = uploadedFile,
                )
            )
            .build()
    }

    /**
     * 统一清理可为空的文本字段，空白内容会被转换为 null。
     */
    private fun normalizeNullableText(value: String): String? {
        return value.trim().ifBlank { null }
    }

    private fun validateAvatarFile(file: MultipartFile) {
        val filename = file.originalFilename
            ?.substringAfterLast('/')
            ?.substringAfterLast('\\')
            .orEmpty()
        val extension = filename.substringAfterLast('.', "").lowercase(Locale.ROOT)
        if (extension !in ALLOWED_AVATAR_EXTENSIONS) {
            throw ParamErrorException("头像文件仅支持 jpg、jpeg、png、gif、webp")
        }

        val contentType = file.contentType?.trim()?.lowercase(Locale.ROOT)
        if (!contentType.isNullOrBlank() && !contentType.startsWith("image/")) {
            throw ParamErrorException("头像文件必须是图片类型")
        }
    }

    private companion object {
        private val ALLOWED_AVATAR_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp")
    }
}

/**
 * 当前登录用户个人资料更新请求。
 */
data class UpdateUserInfoRequest(
    val nickname: String? = null,
    val identity: String? = null,
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

/**
 * 当前登录用户邮箱换绑验证码发送请求。
 */
data class SendChangeEmailCodeRequest(
    val email: String = "",
)

/**
 * 当前登录用户邮箱换绑确认请求。
 */
data class ChangeEmailRequest(
    val email: String = "",
    val verificationCode: String = "",
)
