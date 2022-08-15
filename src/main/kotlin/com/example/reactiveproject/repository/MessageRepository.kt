package com.example.reactiveproject.repository

import com.example.reactiveproject.model.Message
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface MessageRepository: ReactiveMongoRepository<Message, String> {

    fun findMessageById(id: String): Mono<Message>

    fun findMessageByText(text: String): Flux<Message?>
}