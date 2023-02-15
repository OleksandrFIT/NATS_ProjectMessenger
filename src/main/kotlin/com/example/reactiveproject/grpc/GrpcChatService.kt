package com.example.reactiveproject.grpc

import com.example.reactiveproject.ReactorChatServiceGrpc
import com.example.reactiveproject.Services
import com.example.reactiveproject.helper.*
import com.example.reactiveproject.model.Chat
import com.example.reactiveproject.repository.ChatRepository
import com.example.reactiveproject.service.ChatService
import com.google.protobuf.Empty
import io.nats.client.Connection
import org.lognet.springboot.grpc.GRpcService
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.nio.charset.StandardCharsets

@GRpcService
class GrpcChatService: ReactorChatServiceGrpc.ChatServiceImplBase() {
    @Autowired
    lateinit var chatService: ChatService

    @Autowired
    lateinit var chatRepository: ChatRepository

    @Autowired
    lateinit var natsConnection: Connection

    override fun findAllChats(request: Mono<Empty>?): Flux<Services.ChatResponse> {

        //Створюється диспечер який очікує та ловить івент (у мому випадку івент створення нового чату).

        val fluxSink = Flux.create { sink: FluxSink<Chat> ->
            natsConnection.createDispatcher().subscribe("chat-event") { msg ->

                val chat = Services.ChatResponse
                    .parseFrom(msg.data)
                sink.next(grpcToChatUnMono(chat))

                chatRepository.findChatById(msg.data.toString()).map {
                    sink.next(it!!)
                }.subscribe()
            }
        }

        return Flux.merge(chatService.findAllChats(), fluxSink).map {
            chatToGrpcUnMono(it)
        }
    }

    override fun createChat(request: Mono<Services.ChatDescription>?): Mono<Services.ChatResponse> {
        return  grpcToChat(request!!).log("1")
            .flatMap { chatService.createChat(it) }.log("2")
            .map { chatToGrpc(it) }.log("3")
    }

    override fun deleteChat(request: Mono<Services.id>?): Mono<Services.DeleteAnswer> {
        return idToGrpc(request!!)
            .flatMap { chatService.deleteChat(it) }
            .then(Services.DeleteAnswer.newBuilder().apply { text = "Chat is deleted" }.build().toMono())
    }

    override fun addUserToTheChat(request: Mono<Services.ChatUpdateRequest>?): Mono<Services.ChatResponse> {
        return updateGrpcToChat(request!!)
            .flatMap { chatService.addUserToTheChat(it.first, it.second) }
            . map { chatToGrpc(it) }
    }

    override fun deleteUserFromChat(request: Mono<Services.ChatUpdateRequest>?): Mono<Services.ChatResponse> {
        return updateGrpcToChat(request!!)
            .flatMap { chatService.deleteUserFromChat(it.first, it.second) }
            . map { chatToGrpc(it) }
    }

    override fun getChatById(request: Mono<Services.id>?): Mono<Services.FullChatResponse> {
        return idToGrpc(request!!)
            .flatMap { chatService.getChatById(it) }
            .map { fullChatToGrpcUnMono(it) }
    }
}