package top.foxball.nekobackend.datasource.redis

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EmailCodeRepository : CrudRepository<EmailCode, String>
