package top.foxball.nekobackend.service.impl

import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.util.UriComponentsBuilder
import top.foxball.nekobackend.config.FileStorageProperties
import top.foxball.nekobackend.datasource.jdbc.FileMetadata
import top.foxball.nekobackend.datasource.jdbc.FileMetadataRepository
import top.foxball.nekobackend.handlder.ParamErrorException
import top.foxball.nekobackend.handlder.ResourceNotFoundException
import top.foxball.nekobackend.service.FileStorageService
import top.foxball.nekobackend.service.FileUploadResponse
import top.foxball.nekobackend.service.StoredFileResource
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class FileStorageServiceImpl(
    private val properties: FileStorageProperties,
    private val fileMetadataRepository: FileMetadataRepository,
) : FileStorageService {

    @Transactional
    override fun upload(userId: Long, file: MultipartFile): FileUploadResponse {
        if (file.isEmpty) {
            throw ParamErrorException("文件不能为空")
        }

        val today = LocalDate.now()
        val originalFilename = cleanFilename(file.originalFilename)
        val contentType = normalizeContentType(file.contentType)
        val (filename, target) = copyToUniqueTarget(today, originalFilename, file)

        return try {
            val metadata = fileMetadataRepository.saveAndFlush(
                FileMetadata(
                    uploadedByUserId = userId,
                    originalFilename = originalFilename,
                    filename = filename,
                    extension = extensionOf(filename),
                    size = file.size,
                    contentType = contentType,
                    storagePath = target.toString(),
                    relativePath = buildRelativePath(today, filename),
                    downloadUrl = buildDownloadUrl(today, filename),
                    createdAt = LocalDateTime.now(),
                )
            )

            metadata.toUploadResponse()
        } catch (ex: Exception) {
            runCatching { Files.deleteIfExists(target) }
            throw ex
        }
    }

    @Transactional(readOnly = true)
    override fun load(year: Int, month: Int, day: Int, filename: String): StoredFileResource {
        val safeFilename = cleanFilename(filename)
        val relativePath = "/$year/$month/$day/$safeFilename"
        val metadata = fileMetadataRepository.findByRelativePath(relativePath)
            ?: throw ResourceNotFoundException("文件不存在")
        val target = Path.of(metadata.storagePath).toAbsolutePath().normalize()
        validateInsideStorageRoot(target)

        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            throw ResourceNotFoundException("文件不存在")
        }

        return StoredFileResource(
            id = metadata.id ?: throw IllegalStateException("File metadata id is missing."),
            filename = metadata.filename,
            contentType = metadata.contentType ?: Files.probeContentType(target),
            size = metadata.size,
            resource = UrlResource(target.toUri()),
        )
    }

    private fun copyToUniqueTarget(
        date: LocalDate,
        originalFilename: String,
        file: MultipartFile,
    ): Pair<String, Path> {
        val storageDirectory = storageDirectory(date)
        Files.createDirectories(storageDirectory)

        repeat(MAX_FILENAME_ALLOCATE_ATTEMPTS) { attempt ->
            val filename = candidateFilename(originalFilename, attempt)
            val target = storageDirectory.resolve(filename).normalize()
            validateInsideStorageRoot(target)

            try {
                file.inputStream.use { input ->
                    Files.copy(input, target)
                }
                return filename to target
            } catch (ex: java.nio.file.FileAlreadyExistsException) {
                // Try another generated name.
            }
        }

        throw ParamErrorException("文件名冲突，请重试")
    }

    private fun candidateFilename(originalFilename: String, attempt: Int): String {
        if (attempt == 0) {
            return originalFilename
        }

        val (baseName, extension) = splitFilename(originalFilename)
        val suffix = "-${UUID.randomUUID().toString().substring(0, 8)}"
        val extensionLength = extension?.let { it.length + 1 } ?: 0
        val maxBaseLength = MAX_FILENAME_LENGTH - suffix.length - extensionLength
        if (maxBaseLength <= 0) {
            throw ParamErrorException("文件名过长")
        }

        val safeBaseName = baseName.take(maxBaseLength).ifBlank { "file" }
        return if (extension == null) {
            "$safeBaseName$suffix"
        } else {
            "$safeBaseName$suffix.$extension"
        }
    }

    private fun storageDirectory(date: LocalDate): Path {
        return storageRoot()
            .resolve("${date.year}-${date.monthValue}")
            .resolve(date.dayOfMonth.toString())
            .normalize()
    }

    private fun storageRoot(): Path {
        return Path.of(properties.storagePath).toAbsolutePath().normalize()
    }

    private fun validateInsideStorageRoot(target: Path) {
        if (!target.toAbsolutePath().normalize().startsWith(storageRoot())) {
            throw ParamErrorException("文件路径非法")
        }
    }

    private fun buildDownloadUrl(date: LocalDate, filename: String): String {
        return UriComponentsBuilder.fromUriString(normalizedBaseUrl())
            .path("/file/{year}/{month}/{day}/{filename}")
            .buildAndExpand(date.year, date.monthValue, date.dayOfMonth, filename)
            .encode()
            .toUriString()
    }

    private fun buildRelativePath(date: LocalDate, filename: String): String {
        val relativePath = "/${date.year}/${date.monthValue}/${date.dayOfMonth}/$filename"
        if (relativePath.length > MAX_RELATIVE_PATH_LENGTH) {
            throw ParamErrorException("文件路径过长")
        }
        return relativePath
    }

    private fun normalizedBaseUrl(): String {
        val baseUrl = properties.baseUrl.trim().trimEnd('/')
        if (baseUrl.isBlank()) {
            throw IllegalStateException("File base url is not configured.")
        }
        return if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
            baseUrl
        } else {
            "https://$baseUrl"
        }
    }

    private fun cleanFilename(filename: String?): String {
        val rawFilename = filename
            ?.substringAfterLast('/')
            ?.substringAfterLast('\\')
            ?.trim()
            .orEmpty()

        val cleaned = rawFilename
            .replace(INVALID_FILENAME_CHARS, "_")
            .trim('.', ' ')

        val filename = cleaned.ifBlank { "file-${UUID.randomUUID()}" }
        if (filename.length > MAX_FILENAME_LENGTH) {
            throw ParamErrorException("文件名不能超过 $MAX_FILENAME_LENGTH 个字符")
        }

        val extension = extensionOf(filename)
        if (extension != null && extension.length > MAX_EXTENSION_LENGTH) {
            throw ParamErrorException("文件后缀不能超过 $MAX_EXTENSION_LENGTH 个字符")
        }

        return filename
    }

    private fun normalizeContentType(contentType: String?): String? {
        val value = contentType?.trim()?.takeIf { it.isNotBlank() } ?: return null
        if (value.length > MAX_CONTENT_TYPE_LENGTH) {
            throw ParamErrorException("文件类型过长")
        }
        return runCatching {
            org.springframework.http.MediaType.parseMediaType(value).toString()
        }.getOrElse {
            throw ParamErrorException("文件类型不合法")
        }
    }

    private fun extensionOf(filename: String): String? {
        val index = filename.lastIndexOf('.')
        if (index <= 0 || index == filename.lastIndex) {
            return null
        }
        return filename.substring(index + 1).lowercase(Locale.ROOT)
    }

    private fun splitFilename(filename: String): Pair<String, String?> {
        val index = filename.lastIndexOf('.')
        if (index <= 0 || index == filename.lastIndex) {
            return filename to null
        }
        return filename.substring(0, index) to filename.substring(index + 1).lowercase(Locale.ROOT)
    }

    private fun FileMetadata.toUploadResponse(): FileUploadResponse {
        return FileUploadResponse(
            id = id ?: throw IllegalStateException("File metadata id is missing."),
            uploadedByUserId = uploadedByUserId,
            originalFilename = originalFilename,
            filename = filename,
            extension = extension,
            size = size,
            contentType = contentType,
            relativePath = relativePath,
            downloadUrl = downloadUrl,
            createdAt = createdAt,
        )
    }

    private companion object {
        private const val MAX_FILENAME_LENGTH = 255
        private const val MAX_EXTENSION_LENGTH = 32
        private const val MAX_CONTENT_TYPE_LENGTH = 128
        private const val MAX_RELATIVE_PATH_LENGTH = 512
        private const val MAX_FILENAME_ALLOCATE_ATTEMPTS = 8
        private val INVALID_FILENAME_CHARS = Regex("[\\\\/:*?\"<>|\\p{Cntrl}]")
    }
}
