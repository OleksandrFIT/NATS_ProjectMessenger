package com.example.reactiveproject.repository

import com.example.reactiveproject.model.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserRepository: ReactiveMongoRepository<User, String> {

    fun findByName(userName: String): Mono<User?>

    fun findByPhoneNumber(phoneNumber: String): Mono<User?>


}