package top.foxball.nekobackend.service

import org.springframework.stereotype.Service

data class TagResponse(
    val id: Long,
    val name: String,
)

@Service
interface TagService {
    fun listTags(keyword: String?): List<TagResponse>
    fun listUserTags(userId: Long): List<TagResponse>
    fun createAndAddTag(userId: Long, name: String): List<TagResponse>
    fun addExistingTag(userId: Long, tagId: Long): List<TagResponse>
    fun removeTag(userId: Long, tagId: Long): List<TagResponse>
}
