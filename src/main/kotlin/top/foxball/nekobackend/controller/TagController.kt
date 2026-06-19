package top.foxball.nekobackend.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import top.foxball.nekobackend.security.AuthPrincipal
import top.foxball.nekobackend.service.TagResponse
import top.foxball.nekobackend.service.TagService
import top.foxball.nekobackend.shared.Response
import top.foxball.nekobackend.shared.ResponseBuilder

/**
 * 用户标签接口，支持查询全局可复用标签和维护当前用户标签。
 */
@RestController
@RequestMapping("/api")
class TagController(
    private val tagService: TagService,
    private val builder: ResponseBuilder,
) {

    /**
     * 查询全局标签列表，可按关键字搜索已有标签供用户复用。
     */
    @GetMapping("/tags")
    fun listTags(
        @RequestParam(required = false) keyword: String?,
    ): ResponseEntity<Response> {
        data class Response(
            val tags: List<TagResponse>,
        )

        return builder.ok()
            .data(Response(tags = tagService.listTags(keyword)))
            .build()
    }

    /**
     * 查询当前登录用户已添加的标签。
     */
    @GetMapping("/user/tags")
    fun listCurrentUserTags(authentication: Authentication): ResponseEntity<Response> {
        data class Response(
            val tags: List<TagResponse>,
        )

        return builder.ok()
            .data(Response(tags = tagService.listUserTags(currentUserId(authentication))))
            .build()
    }

    /**
     * 创建新标签并添加到当前用户；如果同名标签已存在，则直接复用已有标签。
     */
    @PostMapping("/user/tags")
    fun createAndAddCurrentUserTag(
        authentication: Authentication,
        @RequestBody request: CreateUserTagRequest,
    ): ResponseEntity<Response> {
        data class Response(
            val tags: List<TagResponse>,
        )

        return builder.ok()
            .data(Response(tags = tagService.createAndAddTag(currentUserId(authentication), request.name)))
            .build()
    }

    /**
     * 添加一个已存在的标签到当前用户。
     */
    @PostMapping("/user/tags/{tagId}")
    fun addExistingCurrentUserTag(
        authentication: Authentication,
        @PathVariable tagId: Long,
    ): ResponseEntity<Response> {
        data class Response(
            val tags: List<TagResponse>,
        )

        return builder.ok()
            .data(Response(tags = tagService.addExistingTag(currentUserId(authentication), tagId)))
            .build()
    }

    /**
     * 从当前用户移除一个标签，不会删除全局标签。
     */
    @DeleteMapping("/user/tags/{tagId}")
    fun removeCurrentUserTag(
        authentication: Authentication,
        @PathVariable tagId: Long,
    ): ResponseEntity<Response> {
        data class Response(
            val tags: List<TagResponse>,
        )

        return builder.ok()
            .data(Response(tags = tagService.removeTag(currentUserId(authentication), tagId)))
            .build()
    }

    private fun currentUserId(authentication: Authentication): Long {
        return (authentication.principal as AuthPrincipal).userId
    }
}

data class CreateUserTagRequest(
    val name: String = "",
)
