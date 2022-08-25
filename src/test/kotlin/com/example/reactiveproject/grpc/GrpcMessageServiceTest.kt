//package com.example.reactiveproject.grpc
//
//import com.example.reactiveproject.ReactorMessageServiceGrpc
//import com.example.reactiveproject.ReactorUserServiceGrpc
//import com.example.reactiveproject.Services
//import com.example.reactiveproject.helper.*
//import com.example.reactiveproject.messageUpdateRequest
//import com.example.reactiveproject.model.Message
//import com.example.reactiveproject.repository.MessageRepository
//import com.example.reactiveproject.service.impl.MessageServiceImpl
//import com.example.reactiveproject.service.impl.UserServiceImpl
//import com.google.protobuf.Empty
//import io.grpc.ManagedChannel
//import io.grpc.ManagedChannelBuilder
//import org.bson.types.ObjectId
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.extension.ExtendWith
//import org.mockito.ArgumentMatchers
//import org.mockito.Mockito
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.context.annotation.Import
//import org.springframework.http.HttpHeaders
//import org.springframework.test.context.junit.jupiter.SpringExtension
//import reactor.core.publisher.Flux
//import reactor.core.publisher.Mono
//import reactor.test.StepVerifier
//import java.time.LocalDateTime
//
//@SpringBootTest
//@ExtendWith(SpringExtension::class)
//@Import(MessageServiceImpl::class)
//internal class GrpcMessageServiceTest{
//
//    @MockBean
//    lateinit var messageRepository: MessageRepository
//
//    private val channel: ManagedChannel = ManagedChannelBuilder
//        .forAddress("localhost", 6565)
//        .usePlaintext()
//        .build()
//
//    private var serviceStub: ReactorMessageServiceGrpc.ReactorMessageServiceStub =
//        ReactorMessageServiceGrpc.newReactorStub(channel)
//
//    private fun randomText(): String = List(80) {
//        (('a'..'z')).random()
//    }.joinToString("")
//
//    fun prepareData(): Message {
//        val message = Message(
//            id = ObjectId.get().toString(),
//            datetime = LocalDateTime.now().toString(),
//            text = randomText(),
//            messageChatId = ObjectId.get().toString(),
//            messageUserId = ObjectId.get().toString()
//        )
//        return message
//    }
//
//    @Test
//    fun `should find message by text`() {
//        val message = prepareData()
//        val grpcMessage = messageToGrpcUnMono(message)
//
//        Mockito.`when`(messageRepository.findMessageByText(message.text)).thenReturn(Flux.just(message))
//
//        val request = Services.text.newBuilder().setText(message.text).build()
//        val connect = serviceStub.findMessage(request)
//
//        StepVerifier.create(connect)
//            .expectNext(grpcMessage)
//            .verifyComplete()
//    }
//
//    @Test
//    fun `should create message`() {
//        val message = prepareData()
//        val grpcMessage = messageToGrpcUnMono(message)
//
//        Mockito.`when`(messageRepository.save(ArgumentMatchers.any(Message::class.java)))
//            .thenReturn(Mono.just(message))
//
//        val request =
//            Services.MessageDescription.newBuilder().apply {
//                text = message.text
//                messageChatId = message.messageChatId
//                messageUserId = message.messageUserId
//            }.build()
//
//        val connect = serviceStub.sendMessage(request)
//
//        StepVerifier.create(connect)
//            .expectNext(grpcMessage)
//            .verifyComplete()
//
//    }
//
//    @Test
//    fun `should update message`(){
//        val mesId = ObjectId.get().toString()
//        val date = LocalDateTime.now().toString()
//        val message = Message(
//            id = mesId,
//            datetime = date,
//            text = "ddddddddddd",
//            messageChatId = "1",
//            messageUserId = "1"
//        )
//
//        val messageUp = Message(
//            id = mesId,
//            datetime = date,
//            text = "newwwwwwww",
//            messageChatId = "1",
//            messageUserId = "1"
//        )
//
//        Mockito.`when`(messageRepository.findMessageById(message.id!!)).thenReturn(Mono.just(message))
//        Mockito.`when`(messageRepository.save(ArgumentMatchers.any(Message::class.java))).thenReturn(Mono.just(messageUp))
//
//        val grpcUserRequest = messageToGrpc(message)
//        val request = Services.MessageUpdateRequest
//            .newBuilder()
//            .apply {
//                messageId = mesId
//                messageBuilder.apply {
//                    text = grpcUserRequest.text
//                }
//            }.build()
//
//
//        val connection = serviceStub.editMessage(request)
//        StepVerifier.create(connection)
//            .expectNext(messageToGrpcUnMono(messageUp))
//            .verifyComplete()
//
//    }
//
//
//    @Test
//    fun `should delete message`() {
//        val message = prepareData()
//        val emptyReturn: Mono<Void> = Mono.empty<Void?>()
//        val empty = Empty.newBuilder().build()
//
//        Mockito.`when`(messageRepository.deleteById(message.id!!)).thenReturn(emptyReturn)
//
//        val request = Services.id.newBuilder().setId(message.id).build()
//        val connect = serviceStub.deleteMessage(request)
//
//        val expected = Services.DeleteAnswer.newBuilder().apply { text = "Message is deleted" }.build()
//
//        StepVerifier.create(connect).expectNext(expected).verifyComplete()
//
//    }
//
//}