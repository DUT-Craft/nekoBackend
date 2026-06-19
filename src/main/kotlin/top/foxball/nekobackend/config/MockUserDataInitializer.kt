package top.foxball.nekobackend.config

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import top.foxball.nekobackend.datasource.jdbc.Status
import top.foxball.nekobackend.datasource.jdbc.User
import top.foxball.nekobackend.datasource.jdbc.UserRepository
import java.time.LocalDateTime

/**
 * 应用启动后自动生成用户模拟数据。
 */
@Component
class MockUserDataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val now = LocalDateTime.now()
        val users = (1..10)
            .map { index -> buildMockUser(index, now) }
            .filter { user ->
                userRepository.findByUsername(user.username) == null &&
                    userRepository.findByEmail(user.email) == null
            }

        if (users.isNotEmpty()) {
            userRepository.saveAll(users)
        }
    }

    private fun buildMockUser(index: Int, now: LocalDateTime): User {
        val suffix = index.toString().padStart(2, '0')

        return User(
            username = "mock_user_$suffix",
            password = passwordEncoder.encode(DEFAULT_PASSWORD)
                ?: throw IllegalStateException("Password encoding failed."),
            email = "mock_user_$suffix@example.com",
            nickname = "模拟用户$suffix",
            registerTime = now.minusDays((10 - index).toLong()),
            status = Status.ACTIVE,
            signature = "这是第 $index 个模拟用户。",
            avatar = "https://cdn.jsdelivr.net/gh/sakuranoki/cdn/img/avatar/default.png",
            studentId = "2026$suffix",
            grade = "2026级",
            className = "软件${suffix}班",
            major = "软件工程",
            phone = "138000000$suffix",
            qqNumber = "100000$suffix",
            isStudentId = index % 2 == 0,
            isGrouping = "mock",
            isClassName = true,
            isMajor = true,
            isPhone = false,
            isQQNumber = false,
            contactInformation = mutableListOf("mock_user_$suffix@example.com"),
        )
    }

    private companion object {
        private const val DEFAULT_PASSWORD = "1234567890"
    }
}
