package top.foxball.nekobackend.service

import org.springframework.stereotype.Service
import top.foxball.nekobackend.datasource.jdbc.User

@Service
interface UserService {
    fun findByUsername(username: String): User?
    fun save(user: User): User
    fun findById(id: Long): User?
    fun findByEmail(email: String): User?
    fun createUser(username: String, password: String, email: String): User
}