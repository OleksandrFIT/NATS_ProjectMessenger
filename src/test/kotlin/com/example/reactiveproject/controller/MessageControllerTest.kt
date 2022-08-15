package com.example.reactiveproject.controller


import com.example.reactiveproject.model.Message
import com.example.reactiveproject.repository.MessageRepository
import com.example.reactiveproject.service.impl.MessageServiceImpl
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [MessageController::class])
@Import(MessageServiceImpl::class)
internal class MessageControllerTest{

    @MockBean
    private lateinit var messageRepository: MessageRepository
    @Autowired
    private lateinit var webClient: WebTestClient



    private fun randomText(): String = List(80) {
        (('a'..'z')).random()
    }.joinToString("")

    fun prepareData(): Message {
        val message = Message(
            id = ObjectId.get().toString(),
            datetime = LocalDateTime.now().toString(),
            text = randomText(),
            messageChatId = ObjectId.get().toString(),
            messageUserId = ObjectId.get().toString()
        )
        return message
    }


    @Test
    fun `should create message`(){
        val message = prepareData()

        Mockito.`when`(messageRepository.save(any(Message::class.java))).thenReturn(Mono.just(message))

        webClient.post().uri("http://localhost:8081/message/messages-create")
            .header(HttpHeaders.ACCEPT, "application/json")
            .body(Mono.just(message), Message::class.java)
            .exchange()
            .expectStatus().isCreated
            .expectBody()

        Mockito.verify(messageRepository, Mockito.times(1)).save(any(Message::class.java))
    }

    @Test
    fun `should update user`(){
        val message = prepareData()
        Mockito.`when`(messageRepository.save(any(Message::class.java))).thenReturn(Mono.just(message))
        Mockito.`when`(messageRepository.findMessageById(message.id!!)).thenReturn(Mono.just(message))

        val messageUp = messageRepository.findMessageById(message.id!!)
            .map {
                Message(
                    id = message.id,
                    text = message.text,
                    datetime = LocalDateTime.now().toString())
            }
            .flatMap(
                messageRepository::save
            )
            .subscribe()

        webClient.put().uri("http://localhost:8081/message/messages-edit/{id}", message.id)
            .header(HttpHeaders.ACCEPT, "application/json")
            .body(Mono.just(message), Message::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody()
    }
    @Test
    fun `should delete message`(){
        val message = prepareData()
        val empty: Mono<Void> = Mono.empty()

        Mockito.`when`(messageRepository.deleteById(message.id!!)).thenReturn(empty)

        webClient.delete().uri("http://localhost:8081/message/messages-delete/{id}", message.id)
            .header(HttpHeaders.ACCEPT, "application/json")
            .exchange()
            .expectStatus().isOk

        Mockito.verify(messageRepository, Mockito.times(1)).deleteById(message.id!!)
    }
    @Test
    fun `should find message by text`(){

        val message = prepareData()

        Mockito.`when`(messageRepository.findMessageByText(message.text)).thenReturn(Flux.just(message))

        webClient.get().uri("http://localhost:8081/message/messages/by-text/{text}", message.text)
            .header(HttpHeaders.ACCEPT, "application/json")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Message::class.java)

        Mockito.verify(messageRepository, Mockito.times(1)).findMessageByText(message.text)

    }



}