package top.foxball.nekobackend.service

import org.springframework.stereotype.Service

/**
 * 标签响应数据。
 */
data class TagResponse(
    val id: Long,
    val name: String,
)

/**
 * 标签服务，负责标签查询以及用户标签关系维护。
 */
@Service
interface TagService {
    /**
     * 查询标签列表，可按关键字模糊过滤。
     */
    fun listTags(keyword: String?): List<TagResponse>

    /**
     * 查询指定用户已绑定的标签。
     */
    fun listUserTags(userId: Long): List<TagResponse>

    /**
     * 创建标签并绑定到指定用户。
     *
     * 如果标准化后的同名标签已存在，则复用已有标签。
     */
    fun createAndAddTag(userId: Long, name: String): List<TagResponse>

    /**
     * 将已有标签绑定到指定用户。
     */
    fun addExistingTag(userId: Long, tagId: Long): List<TagResponse>

    /**
     * 从指定用户移除标签绑定。
     */
    fun removeTag(userId: Long, tagId: Long): List<TagResponse>
}
