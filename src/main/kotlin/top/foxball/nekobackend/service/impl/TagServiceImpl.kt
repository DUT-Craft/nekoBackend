package top.foxball.nekobackend.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.nekobackend.datasource.jdbc.Tag
import top.foxball.nekobackend.datasource.jdbc.TagRepository
import top.foxball.nekobackend.datasource.jdbc.User
import top.foxball.nekobackend.datasource.jdbc.UserRepository
import top.foxball.nekobackend.handlder.ParamErrorException
import top.foxball.nekobackend.handlder.ResourceNotFoundException
import top.foxball.nekobackend.handlder.UserNotFoundException
import top.foxball.nekobackend.service.TagResponse
import top.foxball.nekobackend.service.TagService
import top.foxball.nekobackend.service.toTagResponses
import java.time.LocalDateTime
import java.util.*

@Service
class TagServiceImpl(
    private val tagRepository: TagRepository,
    private val userRepository: UserRepository,
) : TagService {

    @Transactional(readOnly = true)
    override fun listTags(keyword: String?): List<TagResponse> {
        val normalizedKeyword = keyword
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotBlank() }

        val tags = if (normalizedKeyword == null) {
            tagRepository.findAllByOrderByNameAsc()
        } else {
            tagRepository.findByNormalizedNameContainingOrderByNameAsc(normalizedKeyword)
        }

        return tags.toTagResponses()
    }

    @Transactional(readOnly = true)
    override fun listUserTags(userId: Long): List<TagResponse> {
        val user = findUser(userId)
        return user.tags.toTagResponses()
    }

    @Transactional
    override fun createAndAddTag(userId: Long, name: String): List<TagResponse> {
        val user = findUser(userId)
        val tagName = cleanTagName(name)
        val normalizedName = normalizeTagName(tagName)
        val tag = tagRepository.findByNormalizedName(normalizedName)
            ?: tagRepository.save(
                Tag(
                    name = tagName,
                    normalizedName = normalizedName,
                    createdByUserId = userId,
                    createdAt = LocalDateTime.now(),
                )
            )

        user.tags.add(tag)
        return userRepository.save(user).tags.toTagResponses()
    }

    @Transactional
    override fun addExistingTag(userId: Long, tagId: Long): List<TagResponse> {
        val user = findUser(userId)
        val tag = tagRepository.findById(tagId)
            .orElseThrow { ResourceNotFoundException("标签不存在") }

        user.tags.add(tag)
        return userRepository.save(user).tags.toTagResponses()
    }

    @Transactional
    override fun removeTag(userId: Long, tagId: Long): List<TagResponse> {
        val user = findUser(userId)
        user.tags.removeIf { it.id == tagId }
        return userRepository.save(user).tags.toTagResponses()
    }

    private fun findUser(userId: Long): User {
        return userRepository.findById(userId).orElseThrow { UserNotFoundException() }
    }

    private fun cleanTagName(name: String): String {
        val tagName = name.trim().replace(WHITESPACE_REGEX, " ")
        if (tagName.isBlank()) {
            throw ParamErrorException("标签名称不能为空")
        }
        if (tagName.length > MAX_TAG_NAME_LENGTH) {
            throw ParamErrorException("标签名称不能超过 $MAX_TAG_NAME_LENGTH 个字符")
        }
        return tagName
    }

    private fun normalizeTagName(name: String): String {
        return name.lowercase(Locale.ROOT)
    }

    private companion object {
        private const val MAX_TAG_NAME_LENGTH = 32
        private val WHITESPACE_REGEX = Regex("\\s+")
    }
}
