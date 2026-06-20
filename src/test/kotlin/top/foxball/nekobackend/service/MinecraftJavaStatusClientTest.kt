package top.foxball.nekobackend.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.foxball.nekobackend.shared.MinecraftJavaStatusClient
import top.foxball.nekobackend.shared.SrvRecord
import top.foxball.nekobackend.shared.SrvResolver
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MinecraftJavaStatusClientTest {
    private lateinit var server: ServerSocket
    private lateinit var executor: ExecutorService

    @BeforeEach
    fun setUp() {
        server = ServerSocket(0)
        executor = Executors.newSingleThreadExecutor()
    }

    @AfterEach
    fun tearDown() {
        server.close()
        executor.shutdownNow()
        executor.awaitTermination(1, TimeUnit.SECONDS)
    }

    @Test
    fun `query reads status response players and latency from explicit address`() {
        val handshakeHost = AtomicReference<String>()
        val future = serveOnce(handshakeHost)
        val client = MinecraftJavaStatusClient(Duration.ofSeconds(2))

        val result = client.query("127.0.0.1:${server.localPort}")

        future.get(1, TimeUnit.SECONDS)
        assertEquals("127.0.0.1", handshakeHost.get())
        assertFalse(result.srvResolved)
        assertEquals("127.0.0.1", result.requestedHost)
        assertEquals("127.0.0.1", result.connectHost)
        assertEquals(server.localPort, result.port)
        assertEquals("1.20.1", result.versionName)
        assertEquals(763, result.protocol)
        assertEquals("Hello Neko", result.motdText)
        assertEquals(2, result.onlinePlayers)
        assertEquals(20, result.maxPlayers)
        assertEquals(listOf("Steve", "Alex"), result.samplePlayers.map { it.name })
        assertTrue(result.latencyMillis >= 0)
        assertNotNull(result.rawJson)
    }

    @Test
    fun `query with srv connects resolved target and keeps original host in handshake`() {
        val handshakeHost = AtomicReference<String>()
        val future = serveOnce(handshakeHost)
        val resolver = SrvResolver { domain ->
            assertEquals("play.example.com", domain)
            SrvRecord(priority = 0, weight = 5, port = server.localPort, target = "127.0.0.1")
        }
        val client = MinecraftJavaStatusClient(Duration.ofSeconds(2), 760, resolver)

        val result = client.queryWithSrv("play.example.com")

        future.get(1, TimeUnit.SECONDS)
        assertTrue(result.srvResolved)
        assertEquals("play.example.com", result.requestedHost)
        assertEquals("127.0.0.1", result.connectHost)
        assertEquals(server.localPort, result.port)
        assertEquals("play.example.com", handshakeHost.get())
        assertEquals("Steve", result.samplePlayers.first().name)
    }

    private fun serveOnce(handshakeHost: AtomicReference<String>): Future<*> {
        return executor.submit {
            server.accept().use { socket ->
                val input = socket.getInputStream()
                val output = socket.getOutputStream()

                val handshake = readPacket(input)
                assertEquals(0x00, readPacketId(handshake))
                handshakeHost.set(readHandshakeHost(handshake))

                val request = readPacket(input)
                assertEquals(0x00, readPacketId(request))

                writeStatusResponse(output, STATUS_JSON)

                val ping = readPacket(input)
                assertEquals(0x01, readPacketId(ping))
                writePong(output, readPingPayload(ping))
            }
        }
    }

    private fun readPacket(input: InputStream): ByteArray {
        val length = readVarInt(input)
        val bytes = input.readNBytes(length)
        if (bytes.size != length) {
            throw EOFException("Unexpected end of packet")
        }
        return bytes
    }

    private fun readPacketId(packet: ByteArray): Int {
        return readVarInt(ByteArrayInputStream(packet))
    }

    private fun readHandshakeHost(packet: ByteArray): String {
        val input = ByteArrayInputStream(packet)
        readVarInt(input)
        readVarInt(input)
        return readString(input)
    }

    private fun readPingPayload(packet: ByteArray): Long {
        val input = ByteArrayInputStream(packet)
        readVarInt(input)
        return DataInputStream(input).readLong()
    }

    private fun writeStatusResponse(output: OutputStream, statusJson: String) {
        writePacket(output) { packet ->
            writeVarInt(packet, 0x00)
            writeString(packet, statusJson)
        }
    }

    private fun writePong(output: OutputStream, payload: Long) {
        writePacket(output) { packet ->
            writeVarInt(packet, 0x01)
            packet.writeLong(payload)
        }
    }

    private fun writePacket(output: OutputStream, block: (DataOutputStream) -> Unit) {
        val packetBuffer = ByteArrayOutputStream()
        val packet = DataOutputStream(packetBuffer)
        block(packet)

        val data = packetBuffer.toByteArray()
        val dataOutput = DataOutputStream(output)
        writeVarInt(dataOutput, data.size)
        dataOutput.write(data)
        dataOutput.flush()
    }

    private fun writeString(output: DataOutputStream, value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        writeVarInt(output, bytes.size)
        output.write(bytes)
    }

    private fun readString(input: InputStream): String {
        val length = readVarInt(input)
        val bytes = input.readNBytes(length)
        if (bytes.size != length) {
            throw EOFException("Unexpected end of string")
        }
        return String(bytes, Charsets.UTF_8)
    }

    private fun writeVarInt(output: DataOutputStream, source: Int) {
        var value = source
        while ((value and 0xFFFFFF80.toInt()) != 0) {
            output.writeByte((value and 0x7F) or 0x80)
            value = value ushr 7
        }
        output.writeByte(value)
    }

    private fun readVarInt(input: InputStream): Int {
        var readCount = 0
        var result = 0

        while (true) {
            val read = input.read()
            if (read == -1) {
                throw EOFException("Unexpected end of VarInt")
            }

            val value = read and 0x7F
            result = result or (value shl (7 * readCount))
            readCount++

            if (readCount > 5) {
                throw IllegalStateException("VarInt is too big")
            }
            if ((read and 0x80) == 0) {
                return result
            }
        }
    }

    private companion object {
        private val STATUS_JSON = """
            {
              "version": {
                "name": "1.20.1",
                "protocol": 763
              },
              "players": {
                "online": 2,
                "max": 20,
                "sample": [
                  {
                    "id": "00000000-0000-0000-0000-000000000001",
                    "name": "Steve"
                  },
                  {
                    "id": "00000000-0000-0000-0000-000000000002",
                    "name": "Alex"
                  }
                ]
              },
              "description": {
                "text": "Hello ",
                "extra": [
                  {
                    "text": "Neko"
                  }
                ]
              },
              "favicon": "data:image/png;base64,abc"
            }
        """.trimIndent()
    }
}
