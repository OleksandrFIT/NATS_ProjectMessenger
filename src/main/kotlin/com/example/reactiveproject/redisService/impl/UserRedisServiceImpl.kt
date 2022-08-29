package com.example.reactiveproject.redisService.impl

import com.example.reactiveproject.Services
import com.example.reactiveproject.helper.grpcResponseToUser
import com.example.reactiveproject.helper.grpcToUser
import com.example.reactiveproject.helper.userToGrpc
import com.example.reactiveproject.helper.userToGrpcUnMonoResponse
import com.example.reactiveproject.model.User
import com.example.reactiveproject.redisService.UserRedisService
import com.example.reactiveproject.repository.UserRepository
import com.example.reactiveproject.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.ReactiveHashOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import javax.annotation.PostConstruct


@Service
class UserRedisServiceImpl: UserRedisService
{
    @Autowired
    private lateinit var redisTemplate: ReactiveStringRedisTemplate
    @Autowired
    private lateinit var reactiveRedisTemplate: ReactiveRedisTemplate<String, Services.UserResponse>
    private lateinit var reactiveHashOps: ReactiveHashOperations<String, String, User>

    private lateinit var hashOperations: HashOperations<String, String, User>

    //упаковываем возвращенные хеш-операции из RedisTemplate экземпляра в HashOperations интерфейс
    @PostConstruct
    fun setUp(){
        reactiveHashOps = redisTemplate.opsForHash()
    }

    val logger: Logger = LoggerFactory.getLogger(UserRepository::class.java)

    override fun createUserCache(user: User): Mono<User> {
        return reactiveRedisTemplate.opsForValue()
            .set(user.id!!, userToGrpcUnMonoResponse(user)).map { user }.doOnSuccess {
                logger
                    .info(String.format("User is CREATED. To find this user, use ID: ${user.id}"))
            }
    }


    override fun deleteUser(id: String): Mono<Void> {
        return redisTemplate.opsForValue().delete(id).then().doOnSuccess {
            logger.info(String.format("User with ID $id is DELETED"))
        }
    }

    override fun updateUser(id: String, user: User): Mono<User> {
        return reactiveRedisTemplate.opsForValue()
            .getAndSet(id, userToGrpcUnMonoResponse(user))
            .map { user }
            .doOnSuccess {
                logger
                    .info(String.format("User is UPDATED. To find updated user, use ID: ${user.id}"))
            }
    }


    override fun findByUserId(id: String): Mono<User?> {
        return grpcResponseToUser(reactiveRedisTemplate.opsForValue().get(id))
            .doOnSuccess {}
    }


}
