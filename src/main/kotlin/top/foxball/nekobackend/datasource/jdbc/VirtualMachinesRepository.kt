package top.foxball.nekobackend.datasource.jdbc

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VirtualMachinesRepository : JpaRepository<VirtualMachines, Long> {
    fun findByPveUserId(pveUserId: Long): List<VirtualMachines>

    fun findByPveUserIdAndPveId(pveUserId: Long, pveId: Long): VirtualMachines?

    fun findByPveId(pveId: Long): List<VirtualMachines>

    fun findByNameContainingIgnoreCase(name: String): List<VirtualMachines>

    fun findByStatus(status: VirtualMachineStatus): List<VirtualMachines>

    fun findByPveUserIdAndStatus(
        pveUserId: Long,
        status: VirtualMachineStatus,
    ): List<VirtualMachines>

    fun existsByPveUserIdAndPveId(pveUserId: Long, pveId: Long): Boolean

    fun deleteByPveUserId(pveUserId: Long): Long
}
