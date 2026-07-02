package top.foxball.nekobackend.shared

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import org.springframework.stereotype.Service
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.time.Duration
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import javax.naming.Context
import javax.naming.NamingException
import javax.naming.directory.DirContext
import javax.naming.directory.InitialDirContext

data class MinecraftJavaServerStatus(
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
    val rawJson: String,
) {
    init {
        require(latencyMillis >= 0) { "latencyMillis must not be negative" }
        normalizeHost(requestedHost)
        normalizeHost(connectHost)
        normalizePort(port)
    }
}

data class MinecraftJavaPlayerSample(
    val id: String?,
    val name: String?,
)

data class SrvRecord(
    val priority: Int,
    val weight: Int,
    val port: Int,
    val target: String,
) {
    init {
        require(priority >= 0) { "priority must not be negative" }
        require(weight >= 0) { "weight must not be negative" }
        normalizePort(port)
        normalizeHost(target)
    }
}

fun interface SrvResolver {
    @Throws(NamingException::class)
    fun resolve(domain: String): SrvRecord?
}

@Service
class MinecraftJavaStatusClient(
    timeout: Duration,
    private val protocolVersion: Int,
    private val srvResolver: SrvResolver,
) {
    private val connectTimeoutMillis: Int = timeout.toTimeoutMillis()
    private val readTimeoutMillis: Int = connectTimeoutMillis

    constructor() : this(DEFAULT_TIMEOUT, DEFAULT_PROTOCOL_VERSION, JndiSrvResolver())

    constructor(timeout: Duration) : this(timeout, DEFAULT_PROTOCOL_VERSION, JndiSrvResolver())

    @Throws(IOException::class)
    fun query(address: String): MinecraftJavaServerStatus {
        val serverAddress = ServerAddress.parse(address)
        return query(serverAddress.host, serverAddress.port)
    }

    @Throws(IOException::class)
    fun query(host: String, port: Int): MinecraftJavaServerStatus {
        val serverAddress = ServerAddress(host = host, port = port, portExplicit = true)
        return queryResolved(
            ResolvedServerAddress(
                handshakeHost = serverAddress.host,
                connectHost = serverAddress.host,
                port = serverAddress.port,
                srvResolved = false,
                srvRecord = null,
            )
        )
    }

    @Throws(IOException::class)
    fun queryWithSrv(domain: String): MinecraftJavaServerStatus {
        val serverAddress = ServerAddress.parse(domain)
        if (serverAddress.portExplicit) {
            return query(serverAddress.host, serverAddress.port)
        }

        return queryResolved(resolveSrvOrDefault(serverAddress.host))
    }

    @Throws(IOException::class)
    fun queryAuto(addressOrDomain: String): MinecraftJavaServerStatus {
        val serverAddress = ServerAddress.parse(addressOrDomain)
        if (serverAddress.portExplicit) {
            return query(serverAddress.host, serverAddress.port)
        }

        return queryWithSrv(serverAddress.host)
    }

    private fun queryResolved(resolvedAddress: ResolvedServerAddress): MinecraftJavaServerStatus {
        Socket().use { socket ->
            socket.connect(
                InetSocketAddress(resolvedAddress.connectHost, resolvedAddress.port),
                connectTimeoutMillis,
            )
            socket.soTimeout = readTimeoutMillis

            val input = socket.getInputStream()
            val output = socket.getOutputStream()

            writePacket(output) { packet ->
                packet.writeVarInt(0x00)
                packet.writeVarInt(protocolVersion)
                packet.writeMinecraftString(resolvedAddress.handshakeHost)
                packet.writeShort(resolvedAddress.port)
                packet.writeVarInt(1)
            }

            writePacket(output) { packet ->
                packet.writeVarInt(0x00)
            }

            input.readVarInt()
            val statusPacketId = input.readVarInt()
            if (statusPacketId != 0x00) {
                throw IOException("Unexpected status packet id: $statusPacketId")
            }

            val statusJson = input.readMinecraftString()

            val pingPayload = System.nanoTime()
            val pingStartedAt = System.nanoTime()
            writePacket(output) { packet ->
                packet.writeVarInt(0x01)
                packet.writeLong(pingPayload)
            }

            input.readVarInt()
            val pongPacketId = input.readVarInt()
            if (pongPacketId != 0x01) {
                throw IOException("Unexpected pong packet id: $pongPacketId")
            }

            val pongPayload = DataInputStream(input).readLong()
            if (pongPayload != pingPayload) {
                throw IOException("Unexpected pong payload")
            }
            val latencyMillis = Duration.ofNanos(System.nanoTime() - pingStartedAt).toMillis()

            return parseStatus(statusJson, resolvedAddress, latencyMillis)
        }
    }

    private fun resolveSrvOrDefault(domain: String): ResolvedServerAddress {
        val srvRecord = try {
            srvResolver.resolve(domain)
        } catch (ex: NamingException) {
            throw IOException("Failed to resolve Minecraft SRV record for $domain", ex)
        }

        if (srvRecord == null) {
            return ResolvedServerAddress(
                handshakeHost = domain,
                connectHost = domain,
                port = DEFAULT_PORT,
                srvResolved = false,
                srvRecord = null,
            )
        }

        return ResolvedServerAddress(
            handshakeHost = domain,
            connectHost = srvRecord.target,
            port = srvRecord.port,
            srvResolved = true,
            srvRecord = srvRecord,
        )
    }

    private fun parseStatus(
        statusJson: String,
        resolvedAddress: ResolvedServerAddress,
        latencyMillis: Long,
    ): MinecraftJavaServerStatus {
        val root = JSON.parseObject(statusJson)
        val version = root.getJSONObject("version")
        val players = root.getJSONObject("players")
        val description = root["description"]
        val samplePlayers = players
            ?.getJSONArray("sample")
            ?.mapNotNull { player ->
                (player as? JSONObject)?.let {
                    MinecraftJavaPlayerSample(
                        id = it.getString("id"),
                        name = it.getString("name"),
                    )
                }
            }
            .orEmpty()

        return MinecraftJavaServerStatus(
            requestedHost = resolvedAddress.handshakeHost,
            connectHost = resolvedAddress.connectHost,
            port = resolvedAddress.port,
            srvResolved = resolvedAddress.srvResolved,
            srvRecord = resolvedAddress.srvRecord,
            versionName = version?.getString("name"),
            protocol = version?.getInteger("protocol"),
            motdText = description?.let(::flattenTextComponent),
            motdRaw = description?.let(JSON::toJSONString),
            onlinePlayers = players?.getInteger("online"),
            maxPlayers = players?.getInteger("max"),
            samplePlayers = samplePlayers,
            latencyMillis = latencyMillis,
            favicon = root.getString("favicon"),
            rawJson = statusJson,
        )
    }

    private data class ServerAddress(
        val host: String,
        val port: Int,
        val portExplicit: Boolean,
    ) {
        init {
            normalizeHost(host)
            normalizePort(port)
        }

        companion object {
            fun parse(address: String): ServerAddress {
                val value = address.trim()
                require(value.isNotBlank()) { "address must not be blank" }

                if (value.startsWith("[")) {
                    val closingIndex = value.indexOf(']')
                    require(closingIndex >= 0) { "invalid IPv6 address" }

                    val host = value.substring(1, closingIndex)
                    val rest = value.substring(closingIndex + 1)
                    if (rest.isBlank()) {
                        return ServerAddress(host = host, port = DEFAULT_PORT, portExplicit = false)
                    }
                    require(rest.startsWith(":")) { "invalid address" }
                    return ServerAddress(host = host, port = parsePort(rest.substring(1)), portExplicit = true)
                }

                val firstColon = value.indexOf(':')
                val lastColon = value.lastIndexOf(':')
                if (firstColon > -1 && firstColon == lastColon) {
                    return ServerAddress(
                        host = value.substring(0, firstColon),
                        port = parsePort(value.substring(firstColon + 1)),
                        portExplicit = true,
                    )
                }

                return ServerAddress(host = value, port = DEFAULT_PORT, portExplicit = false)
            }

            private fun parsePort(value: String): Int {
                return try {
                    normalizePort(value.toInt())
                } catch (ex: NumberFormatException) {
                    throw IllegalArgumentException("invalid port: $value", ex)
                }
            }
        }
    }

    private data class ResolvedServerAddress(
        val handshakeHost: String,
        val connectHost: String,
        val port: Int,
        val srvResolved: Boolean,
        val srvRecord: SrvRecord?,
    ) {
        init {
            normalizeHost(handshakeHost)
            normalizeHost(connectHost)
            normalizePort(port)
        }
    }

    private class JndiSrvResolver : SrvResolver {
        override fun resolve(domain: String): SrvRecord? {
            val normalizedDomain = normalizeHost(domain)
            val env = Hashtable<String, String>()
            env[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.dns.DnsContextFactory"

            var context: DirContext? = null
            return try {
                context = InitialDirContext(env)
                val attributes = context.getAttributes("$SRV_PREFIX$normalizedDomain", arrayOf("SRV"))
                val attribute = attributes.get("SRV") ?: return null
                if (attribute.size() == 0) {
                    return null
                }

                val records = (0 until attribute.size())
                    .map { parseSrvRecord(attribute.get(it).toString()) }

                chooseSrvRecord(records)
            } catch (ex: NamingException) {
                null
            } finally {
                try {
                    context?.close()
                } catch (ignored: NamingException) {
                    // Resolver falls back to the default port when SRV lookup is unavailable.
                }
            }
        }

        private fun parseSrvRecord(value: String): SrvRecord {
            val parts = value.trim().split(Regex("\\s+"))
            require(parts.size == 4) { "invalid SRV record: $value" }

            return SrvRecord(
                priority = parts[0].toInt(),
                weight = parts[1].toInt(),
                port = parts[2].toInt(),
                target = normalizeHost(parts[3]),
            )
        }

        private fun chooseSrvRecord(records: List<SrvRecord>): SrvRecord? {
            if (records.isEmpty()) {
                return null
            }

            val bestPriority = records.minOf { it.priority }
            val candidates = records.filter { it.priority == bestPriority }
            val totalWeight = candidates.sumOf { it.weight }

            if (totalWeight <= 0) {
                return candidates[ThreadLocalRandom.current().nextInt(candidates.size)]
            }

            val selectedWeight = ThreadLocalRandom.current().nextInt(totalWeight)
            var currentWeight = 0
            for (candidate in candidates) {
                currentWeight += candidate.weight
                if (selectedWeight < currentWeight) {
                    return candidate
                }
            }

            return candidates.last()
        }
    }

    private companion object {
        private const val DEFAULT_PORT = 25565
        private const val DEFAULT_PROTOCOL_VERSION = 760
        private const val SRV_PREFIX = "_minecraft._tcp."
        private val DEFAULT_TIMEOUT: Duration = Duration.ofSeconds(5)
    }
}

private fun flattenTextComponent(value: Any?): String {
    return when (value) {
        null -> ""
        is String -> value
        is JSONArray -> value.joinToString(separator = "") { flattenTextComponent(it) }
        is JSONObject -> buildString {
            value.getString("text")?.let(::append)
            value["extra"]?.let { append(flattenTextComponent(it)) }
        }

        else -> value.toString()
    }
}

private fun writePacket(output: OutputStream, writer: (DataOutputStream) -> Unit) {
    val packetBuffer = ByteArrayOutputStream()
    val packet = DataOutputStream(packetBuffer)
    writer(packet)

    val packetBytes = packetBuffer.toByteArray()
    val dataOutput = DataOutputStream(output)
    dataOutput.writeVarInt(packetBytes.size)
    dataOutput.write(packetBytes)
    dataOutput.flush()
}

private fun DataOutputStream.writeMinecraftString(value: String) {
    val bytes = value.toByteArray(Charsets.UTF_8)
    writeVarInt(bytes.size)
    write(bytes)
}

private fun InputStream.readMinecraftString(): String {
    val length = readVarInt()
    val bytes = readNBytes(length)
    if (bytes.size != length) {
        throw EOFException("Unexpected end of Minecraft string")
    }
    return String(bytes, Charsets.UTF_8)
}

private fun DataOutputStream.writeVarInt(source: Int) {
    var value = source
    while ((value and 0xFFFFFF80.toInt()) != 0) {
        writeByte((value and 0x7F) or 0x80)
        value = value ushr 7
    }
    writeByte(value)
}

private fun InputStream.readVarInt(): Int {
    var readCount = 0
    var result = 0

    while (true) {
        val read = read()
        if (read == -1) {
            throw EOFException("Unexpected end of VarInt")
        }

        val value = read and 0x7F
        result = result or (value shl (7 * readCount))
        readCount++

        if (readCount > 5) {
            throw IOException("VarInt is too big")
        }
        if ((read and 0x80) == 0) {
            return result
        }
    }
}

private fun Duration.toTimeoutMillis(): Int {
    require(!isZero && !isNegative) { "timeout must be positive" }

    val millis = toMillis()
    require(millis > 0) { "timeout must be at least 1 millisecond" }
    require(millis <= Int.MAX_VALUE) { "timeout is too large" }
    return millis.toInt()
}

private fun normalizeHost(value: String): String {
    val host = value.trim().removeSuffix(".")
    require(host.isNotBlank()) { "host must not be blank" }
    return host
}

private fun normalizePort(value: Int): Int {
    require(value in 1..65535) { "port must be between 1 and 65535" }
    return value
}
