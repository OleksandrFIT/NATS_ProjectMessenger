package com.example.reactiveproject.redisService

import com.example.reactiveproject.model.User
import reactor.core.publisher.Mono

interface UserRedisService {
    fun createUserCache(user: User): Mono<User>

    fun deleteUser(id: String): Mono<Void>

    fun updateUser(id: String, user: User): Mono<User>

    fun findByUserId(id: String): Mono<User?>
}