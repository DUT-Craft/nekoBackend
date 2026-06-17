package top.foxball.nekobackend.service.impl

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import top.foxball.nekobackend.datasource.jdbc.Status
import top.foxball.nekobackend.datasource.jdbc.User
import top.foxball.nekobackend.datasource.jdbc.UserRepository
import top.foxball.nekobackend.service.UserService
import java.time.LocalDateTime

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
): UserService {

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

    override fun createUser(username: String, password: String, email: String): User {
        return userRepository.save(User(
            username = username,
            password = passwordEncoder.encode(password),
            email = email,
            nickname = username,
            registerTime = LocalDateTime.now(),
            status = Status.ACTIVE,
            signature = "This is a signature.",
            avatar = "https://cdn.jsdelivr.net/gh/sakuranoki/cdn/img/avatar/default.png",
        ))
    }
}
