package top.foxball.nekobackend.service.impl

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.nekobackend.datasource.jdbc.Status
import top.foxball.nekobackend.datasource.jdbc.User
import top.foxball.nekobackend.datasource.jdbc.UserRepository
import top.foxball.nekobackend.handlder.UserAlreadyExistsException
import top.foxball.nekobackend.service.UserService
import java.time.LocalDateTime

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : UserService {

    override fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    override fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    override fun save(user: User): User {
        return userRepository.save(user)
    }

    override fun findById(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }

    @Transactional
    override fun createUser(username: String, password: String, email: String): User {
        val normalizedUsername = username.trim()
        val normalizedEmail = email.trim()

        if (userRepository.findByUsername(normalizedUsername) != null) {
            throw UserAlreadyExistsException("用户名已存在")
        }
        if (userRepository.findByEmail(normalizedEmail) != null) {
            throw UserAlreadyExistsException("邮箱已存在")
        }

        return userRepository.save(
            User(
                username = normalizedUsername,
                password = passwordEncoder.encode(password)
                    ?: throw IllegalStateException("Password encoding failed."),
                email = normalizedEmail,
                nickname = normalizedUsername,
                registerTime = LocalDateTime.now(),
                status = Status.ACTIVE,
                signature = "This is a signature.",
                avatar = "https://cdn.jsdelivr.net/gh/sakuranoki/cdn/img/avatar/default.png",
            )
        )
    }
}
