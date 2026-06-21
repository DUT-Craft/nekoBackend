package top.foxball.nekobackend.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.nekobackend.datasource.jdbc.PveUser
import top.foxball.nekobackend.datasource.jdbc.PveUserRepository
import top.foxball.nekobackend.datasource.jdbc.VirtualMachineStatus
import top.foxball.nekobackend.datasource.jdbc.VirtualMachines
import top.foxball.nekobackend.datasource.jdbc.VirtualMachinesRepository
import top.foxball.nekobackend.handlder.ParamErrorException
import top.foxball.nekobackend.handlder.ResourceNotFoundException
import top.foxball.nekobackend.service.PveManagementService
import top.foxball.nekobackend.service.PveUserCreateRequest
import top.foxball.nekobackend.service.PveUserResponse
import top.foxball.nekobackend.service.PveUserUpdateRequest
import top.foxball.nekobackend.service.VirtualMachineCreateRequest
import top.foxball.nekobackend.service.VirtualMachineResponse
import top.foxball.nekobackend.service.VirtualMachineStatusUpdateRequest
import top.foxball.nekobackend.service.VirtualMachineUpdateRequest

@Service
class PveManagementServiceImpl(
    private val pveUserRepository: PveUserRepository,
    private val virtualMachinesRepository: VirtualMachinesRepository,
) : PveManagementService {

    @Transactional
    override fun createPveUser(request: PveUserCreateRequest): PveUserResponse {
        val now = System.currentTimeMillis()
        val pveUser = PveUser().apply {
            userId = request.userId
            username = requireText(request.username, "PVE 用户名不能为空")
            password = normalizeNullableText(request.password)
            address = requireText(request.address, "PVE 地址不能为空")
            port = normalizePort(request.port)
            pam = normalizeNullableText(request.pam)
            email = normalizeNullableText(request.email)
            createdAt = now
            updatedAt = now
            status = normalizeNullableText(request.status) ?: "ACTIVE"
            apiToken = normalizeNullableText(request.apiToken)
        }

        return pveUserRepository.save(pveUser).toResponse()
    }

    @Transactional(readOnly = true)
    override fun listPveUsers(
        userId: Long?,
        username: String?,
        email: String?,
        status: String?,
        address: String?,
    ): List<PveUserResponse> {
        val normalizedUsername = normalizeNullableText(username)
        val normalizedEmail = normalizeNullableText(email)
        val normalizedStatus = normalizeNullableText(status)
        val normalizedAddress = normalizeNullableText(address)

        return pveUserRepository.findAll()
            .asSequence()
            .filter { userId == null || it.userId == userId }
            .filter { normalizedUsername == null || it.username?.contains(normalizedUsername, ignoreCase = true) == true }
            .filter { normalizedEmail == null || it.email?.equals(normalizedEmail, ignoreCase = true) == true }
            .filter { normalizedStatus == null || it.status?.equals(normalizedStatus, ignoreCase = true) == true }
            .filter { normalizedAddress == null || it.address?.contains(normalizedAddress, ignoreCase = true) == true }
            .map { it.toResponse() }
            .toList()
    }

    @Transactional(readOnly = true)
    override fun getPveUser(id: Long): PveUserResponse {
        return findPveUser(id).toResponse()
    }

    @Transactional
    override fun updatePveUser(id: Long, request: PveUserUpdateRequest): PveUserResponse {
        val pveUser = findPveUser(id)

        request.userId?.let { pveUser.userId = it }
        request.username?.let { pveUser.username = requireText(it, "PVE 用户名不能为空") }
        request.password?.let { pveUser.password = normalizeNullableText(it) }
        request.address?.let { pveUser.address = requireText(it, "PVE 地址不能为空") }
        request.port?.let { pveUser.port = normalizePort(it) }
        request.pam?.let { pveUser.pam = normalizeNullableText(it) }
        request.email?.let { pveUser.email = normalizeNullableText(it) }
        request.status?.let { pveUser.status = normalizeNullableText(it) }
        request.apiToken?.let { pveUser.apiToken = normalizeNullableText(it) }
        pveUser.updatedAt = System.currentTimeMillis()

        return pveUserRepository.save(pveUser).toResponse()
    }

    @Transactional
    override fun deletePveUser(id: Long) {
        val pveUser = findPveUser(id)
        virtualMachinesRepository.deleteByPveUserId(id)
        pveUserRepository.delete(pveUser)
    }

    @Transactional
    override fun createVirtualMachine(request: VirtualMachineCreateRequest): VirtualMachineResponse {
        request.pveUserId?.let { ensurePveUserExists(it) }
        val virtualMachine = VirtualMachines().apply {
            pveId = request.pveId
            name = requireText(request.name, "虚拟机名称不能为空")
            description = normalizeNullableText(request.description)
            status = request.status ?: VirtualMachineStatus.UNKNOWN
            createdAt = System.currentTimeMillis()
            pveUserId = request.pveUserId
            systemUserName = normalizeNullableText(request.systemUserName)
            systemUserPassword = normalizeNullableText(request.systemUserPassword)
            publicKey = normalizeNullableText(request.publicKey)
            privateKey = normalizeNullableText(request.privateKey)
        }

        return virtualMachinesRepository.save(virtualMachine).toResponse()
    }

    @Transactional(readOnly = true)
    override fun listVirtualMachines(
        pveUserId: Long?,
        pveId: Long?,
        name: String?,
        status: VirtualMachineStatus?,
    ): List<VirtualMachineResponse> {
        val normalizedName = normalizeNullableText(name)

        return virtualMachinesRepository.findAll()
            .asSequence()
            .filter { pveUserId == null || it.pveUserId == pveUserId }
            .filter { pveId == null || it.pveId == pveId }
            .filter { normalizedName == null || it.name?.contains(normalizedName, ignoreCase = true) == true }
            .filter { status == null || it.status == status }
            .map { it.toResponse() }
            .toList()
    }

    @Transactional(readOnly = true)
    override fun getVirtualMachine(id: Long): VirtualMachineResponse {
        return findVirtualMachine(id).toResponse()
    }

    @Transactional
    override fun updateVirtualMachine(id: Long, request: VirtualMachineUpdateRequest): VirtualMachineResponse {
        val virtualMachine = findVirtualMachine(id)

        request.pveUserId?.let {
            ensurePveUserExists(it)
            virtualMachine.pveUserId = it
        }
        request.pveId?.let { virtualMachine.pveId = it }
        request.name?.let { virtualMachine.name = requireText(it, "虚拟机名称不能为空") }
        request.description?.let { virtualMachine.description = normalizeNullableText(it) }
        request.status?.let { virtualMachine.status = it }
        request.systemUserName?.let { virtualMachine.systemUserName = normalizeNullableText(it) }
        request.systemUserPassword?.let { virtualMachine.systemUserPassword = normalizeNullableText(it) }
        request.publicKey?.let { virtualMachine.publicKey = normalizeNullableText(it) }
        request.privateKey?.let { virtualMachine.privateKey = normalizeNullableText(it) }

        return virtualMachinesRepository.save(virtualMachine).toResponse()
    }

    @Transactional
    override fun updateVirtualMachineStatus(
        id: Long,
        request: VirtualMachineStatusUpdateRequest,
    ): VirtualMachineResponse {
        val virtualMachine = findVirtualMachine(id)
        virtualMachine.status = request.status

        return virtualMachinesRepository.save(virtualMachine).toResponse()
    }

    @Transactional
    override fun deleteVirtualMachine(id: Long) {
        val virtualMachine = findVirtualMachine(id)
        virtualMachinesRepository.delete(virtualMachine)
    }

    private fun findPveUser(id: Long): PveUser {
        return pveUserRepository.findById(id).orElseThrow { ResourceNotFoundException("PVE 用户不存在") }
    }

    private fun findVirtualMachine(id: Long): VirtualMachines {
        return virtualMachinesRepository.findById(id).orElseThrow { ResourceNotFoundException("虚拟机不存在") }
    }

    private fun ensurePveUserExists(id: Long) {
        if (!pveUserRepository.existsById(id)) {
            throw ResourceNotFoundException("PVE 用户不存在")
        }
    }

    private fun requireText(value: String, message: String): String {
        return value.trim().ifBlank { throw ParamErrorException(message) }
    }

    private fun normalizeNullableText(value: String?): String? {
        return value?.trim()?.ifBlank { null }
    }

    private fun normalizePort(value: String?): String {
        val port = normalizeNullableText(value) ?: "8006"
        val portNumber = port.toIntOrNull() ?: throw ParamErrorException("PVE 端口必须是数字")
        if (portNumber !in 1..65535) {
            throw ParamErrorException("PVE 端口必须在 1 到 65535 之间")
        }
        return port
    }

    private fun PveUser.toResponse(): PveUserResponse {
        return PveUserResponse(
            id = id,
            userId = userId,
            username = username,
            address = address,
            port = port,
            pam = pam,
            email = email,
            createdAt = createdAt,
            updatedAt = updatedAt,
            status = status,
            hasPassword = !password.isNullOrBlank(),
            hasApiToken = !apiToken.isNullOrBlank(),
        )
    }

    private fun VirtualMachines.toResponse(): VirtualMachineResponse {
        return VirtualMachineResponse(
            id = id,
            pveId = pveId,
            name = name,
            description = description,
            status = status,
            createdAt = createdAt,
            pveUserId = pveUserId,
            systemUserName = systemUserName,
            hasSystemUserPassword = !systemUserPassword.isNullOrBlank(),
            hasPublicKey = !publicKey.isNullOrBlank(),
            hasPrivateKey = !privateKey.isNullOrBlank(),
        )
    }
}
