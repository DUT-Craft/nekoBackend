package top.foxball.nekobackend.controller

import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import top.foxball.nekobackend.security.AuthPrincipal
import top.foxball.nekobackend.service.FileStorageService
import top.foxball.nekobackend.shared.Response
import top.foxball.nekobackend.shared.ResponseBuilder
import java.nio.charset.StandardCharsets

/** 文件上传和公开下载接口。 */
@RestController
class FileController(
    private val fileStorageService: FileStorageService,
    private val builder: ResponseBuilder,
) {

    /** 上传文件，返回可公开访问的下载地址。 */
    @PostMapping("/api/files/upload")
    fun upload(
        authentication: Authentication,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<Response> {
        val principal = authentication.principal as AuthPrincipal
        return builder.ok()
            .data(fileStorageService.upload(principal.userId, file))
            .build()
    }

    /** 公开下载文件。 */
    @GetMapping("/file/{year}/{month}/{day}/{filename:.+}")
    fun download(
        @PathVariable year: Int,
        @PathVariable month: Int,
        @PathVariable day: Int,
        @PathVariable filename: String,
    ): ResponseEntity<StreamingResponseBody> {
        val storedFile = fileStorageService.load(year, month, day, filename)
        val contentType = runCatching {
            storedFile.contentType?.let { MediaType.parseMediaType(it) }
        }.getOrNull()
            ?: MediaType.APPLICATION_OCTET_STREAM
        val body = StreamingResponseBody { output ->
            storedFile.resource.inputStream.use { input ->
                input.copyTo(output)
            }
        }

        return ResponseEntity.ok()
            .contentType(contentType)
            .contentLength(storedFile.size)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.inline()
                    .filename(storedFile.filename, StandardCharsets.UTF_8)
                    .build()
                    .toString()
            )
            .body(body)
    }
}
