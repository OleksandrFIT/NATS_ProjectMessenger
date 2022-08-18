package com.example.reactiveproject.controller

import com.example.reactiveproject.model.Message
import com.example.reactiveproject.service.MessageService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/message")
class MessageController(
//    @Qualifier("messageRedisServiceImpl")
    val messageService: MessageService
) {
    @PostMapping("/messages-create")
    @ResponseStatus(HttpStatus.CREATED)
    fun sendMessage(@RequestBody message: Message):Mono<Message> {
       return messageService.sendMessage(message)
    }

    @PutMapping("/messages-edit/{id}")
    fun editMessage(@PathVariable("id") id: String, @RequestBody message: Message): Mono<Message> {
       return messageService.editMessage(id, message)
    }

    @DeleteMapping("/messages-delete/{id}")
    fun delete(@PathVariable("id") id: String): Mono<Void> {
        return messageService.deleteMessage(id)
    }

    @GetMapping("/messages/by-text/{text}")
    fun findMessage(@PathVariable("text") text: String): Flux<Message?> {
        return messageService.findMessage(text)
    }
}