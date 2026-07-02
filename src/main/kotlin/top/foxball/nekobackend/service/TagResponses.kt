package top.foxball.nekobackend.service

import top.foxball.nekobackend.datasource.jdbc.Tag
import java.util.*

/**
 * 将标签实体集合转换为按名称排序的响应数据。
 */
fun Collection<Tag>.toTagResponses(): List<TagResponse> {
    return sortedWith(compareBy<Tag> { it.name.lowercase(Locale.ROOT) }.thenBy { it.id ?: Long.MAX_VALUE })
        .map { tag ->
            TagResponse(
                id = tag.id ?: throw IllegalStateException("Tag id is missing."),
                name = tag.name,
            )
        }
}
