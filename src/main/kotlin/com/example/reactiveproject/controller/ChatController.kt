package com.example.reactiveproject.controller

import com.example.reactiveproject.model.Chat
import com.example.reactiveproject.model.FullChat
import com.example.reactiveproject.service.ChatService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/chat")
class ChatController (
    private val chatService: ChatService
){

    @PostMapping("/chats-create")
    @ResponseStatus(HttpStatus.CREATED)
    fun createChat(@RequestBody chat: Chat) {
        chatService.createChat(chat)
    }

    @PutMapping("/chats-add/{chatId}/{id}")
    fun addUserToTheChat(@PathVariable("chatId") chatId: String, @PathVariable("id") userId: String) {
        chatService.addUserToTheChat(chatId, userId)
    }

    @DeleteMapping("/chats-delete/{id}")
    fun delete(@PathVariable("id") id: String) {
        chatService.deleteChat(id)
    }

    @GetMapping("/chats-all")
    @ResponseStatus(HttpStatus.FOUND)
    fun findAllChats(): Flux<Chat> {
        return chatService.findAllChats()
    }

    @PutMapping("/chats-remove/{chatId}/{id}")
    fun deleteUserFromChat(@PathVariable("chatId") chatId: String, @PathVariable("id") userId: String){
        chatService.deleteUserFromChat(chatId, userId)
    }

    @GetMapping("/get-chat-id/{id}")
    fun getChatById(@PathVariable(value = "id") chatId: String): Mono<FullChat> {
       return chatService.getChatById(chatId)
    }

}
