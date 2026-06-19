package top.foxball.nekobackend.service

import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

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

data class StoredFileResource(
    val id: Long,
    val filename: String,
    val contentType: String?,
    val size: Long,
    val resource: Resource,
)

@Service
interface FileStorageService {
    fun upload(userId: Long, file: MultipartFile): FileUploadResponse
    fun load(year: Int, month: Int, day: Int, filename: String): StoredFileResource
}
