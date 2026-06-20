package top.foxball.nekobackend.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import top.foxball.nekobackend.handlder.ParamErrorException
import top.foxball.nekobackend.shared.MinecraftJavaPlayerSample
import top.foxball.nekobackend.shared.MinecraftJavaServerStatus
import top.foxball.nekobackend.shared.MinecraftJavaStatusClient
import top.foxball.nekobackend.shared.SrvRecord
import top.foxball.nekobackend.shared.Response
import top.foxball.nekobackend.shared.ResponseBuilder
import java.io.IOException

data class MinecraftJavaStatusQueryResponse(
    val mode: MinecraftJavaStatusQueryMode,
    val requestedHost: String,
    val connectHost: String,
    val port: Int,
    val srvResolved: Boolean,
    val srvRecord: SrvRecord?,
    val versionName: String?,
    val protocol: Int?,
    val motdText: String?,
    val motdRaw: String?,
    val onlinePlayers: Int?,
    val maxPlayers: Int?,
    val samplePlayers: List<MinecraftJavaPlayerSample>,
    val latencyMillis: Long,
    val favicon: String?,
)

enum class MinecraftJavaStatusQueryMode {
    AUTO,
    DIRECT,
    SRV,
    ;

    companion object {
        fun from(value: String): MinecraftJavaStatusQueryMode {
            return entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: throw ParamErrorException("查询模式仅支持 AUTO、DIRECT、SRV")
        }
    }
}

@RestController
@RequestMapping("/api/minecraft/java/status")
class MinecraftJavaStatusController(
    private val minecraftJavaStatusClient: MinecraftJavaStatusClient,
    private val builder: ResponseBuilder,
) {

    @GetMapping
    fun query(
        @RequestParam address: String,
        @RequestParam(defaultValue = "AUTO") mode: String,
    ): ResponseEntity<Response> {
        val normalizedAddress = address.trim().ifBlank {
            throw ParamErrorException("服务端地址不能为空")
        }
        val queryMode = MinecraftJavaStatusQueryMode.from(mode)
        val status = try {
            when (queryMode) {
                MinecraftJavaStatusQueryMode.AUTO -> minecraftJavaStatusClient.queryAuto(normalizedAddress)
                MinecraftJavaStatusQueryMode.DIRECT -> minecraftJavaStatusClient.query(normalizedAddress)
                MinecraftJavaStatusQueryMode.SRV -> minecraftJavaStatusClient.queryWithSrv(normalizedAddress)
            }
        } catch (ex: IOException) {
            return builder.serviceUnavailable()
                .message("Minecraft 服务端状态查询失败：${ex.message ?: "连接失败"}")
                .build()
        }

        return builder.ok()
            .data(status.toQueryResponse(queryMode))
            .build()
    }

    private fun MinecraftJavaServerStatus.toQueryResponse(
        mode: MinecraftJavaStatusQueryMode,
    ): MinecraftJavaStatusQueryResponse {
        return MinecraftJavaStatusQueryResponse(
            mode = mode,
            requestedHost = requestedHost,
            connectHost = connectHost,
            port = port,
            srvResolved = srvResolved,
            srvRecord = srvRecord,
            versionName = versionName,
            protocol = protocol,
            motdText = motdText,
            motdRaw = motdRaw,
            onlinePlayers = onlinePlayers,
            maxPlayers = maxPlayers,
            samplePlayers = samplePlayers,
            latencyMillis = latencyMillis,
            favicon = favicon,
        )
    }
}
