package top.foxball.nekobackend.service

import org.springframework.stereotype.Service
import top.foxball.nekobackend.datasource.jdbc.User

/**
 * 用户基础服务，封装用户查询、保存和创建能力。
 */
@Service
interface UserService {
    /**
     * 按用户名查询用户。
     */
    fun findByUsername(username: String): User?

    /**
     * 保存用户实体。
     */
    fun save(user: User): User

    /**
     * 按主键查询用户。
     */
    fun findById(id: Long): User?

    /**
     * 按邮箱查询用户。
     */
    fun findByEmail(email: String): User?

    /**
     * 创建用户并加密保存密码。
     */
    fun createUser(username: String, password: String, email: String): User
}
