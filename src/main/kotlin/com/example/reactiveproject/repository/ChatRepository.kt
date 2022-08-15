package com.example.reactiveproject.repository

import com.example.reactiveproject.model.Chat
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface ChatRepository: ReactiveMongoRepository<Chat, String> {

    fun findByName(name: String): Mono<Chat>

    fun findChatById(id: String): Mono<Chat?>


}