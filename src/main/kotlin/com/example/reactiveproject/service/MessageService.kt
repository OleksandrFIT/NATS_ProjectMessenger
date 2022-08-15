package com.example.reactiveproject.service

import com.example.reactiveproject.model.Message
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface MessageService {

    fun sendMessage(message: Message): Mono<Message>

    fun deleteMessage(messageId: String): Mono<Void>

    fun editMessage(id: String, message: Message): Mono<Message>

    fun findMessage(text: String): Flux<Message?>

}