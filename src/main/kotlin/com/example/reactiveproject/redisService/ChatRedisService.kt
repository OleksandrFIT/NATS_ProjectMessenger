package com.example.reactiveproject.redisService

import com.example.reactiveproject.model.Chat
import com.example.reactiveproject.model.FullChat
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ChatRedisService {

    fun createChat(chat: Chat): Mono<Chat>

    fun deleteChat(id: String): Mono<Void>

    fun addUserToTheChat(chatId: String, userId: String): Mono<Chat>

    fun deleteUserFromChat(chatId: String, userId: String): Mono<Chat>



}