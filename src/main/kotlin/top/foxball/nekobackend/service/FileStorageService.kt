package top.foxball.nekobackend.service

import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * 文件上传成功后的元数据。
 */
data class FileUploadResponse(
    val id: Long,
    val uploadedByUserId: Long?,
    val originalFilename: String,
    val filename: String,
    val extension: String?,
    val size: Long,
    val contentType: String?,
    val relativePath: String,
    val downloadUrl: String,
    val createdAt: java.time.LocalDateTime?,
)

/**
 * 已存储文件的下载资源和响应头所需元数据。
 */
data class StoredFileResource(
    val id: Long,
    val filename: String,
    val contentType: String?,
    val size: Long,
    val resource: Resource,
)

/**
 * 文件存储服务，负责上传文件落盘、保存元数据和按日期路径加载文件。
 */
@Service
interface FileStorageService {
    /**
     * 上传文件并返回可下载地址。
     *
     * 会清理文件名、校验 Content-Type，并确保文件写入到配置的存储根目录内。
     */
    fun upload(userId: Long, file: MultipartFile): FileUploadResponse

    /**
     * 根据下载路径中的日期和文件名加载已存储文件。
     */
    fun load(year: Int, month: Int, day: Int, filename: String): StoredFileResource
}
