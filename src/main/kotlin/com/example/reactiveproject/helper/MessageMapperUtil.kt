package com.example.reactiveproject.helper

import com.example.reactiveproject.Services
import com.example.reactiveproject.model.Chat
import com.example.reactiveproject.model.Message
import org.bson.types.ObjectId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.LocalDateTime


fun textToGrpc(grpcText: Mono<Services.text>): Mono<String>{
    return grpcText.map {
        it.text.toString()
    }
}

fun monoToFlux(message: Message): Flux<Message>{
    return Flux.from(Mono.just(message))
}

fun textToGrpcUnMono(grpcText: Mono<Services.text>): String{
    return grpcText.block()!!.text.toString()
}


fun grpcToMessage(message: Mono<Services.MessageDescription>): Mono<Message> {
    return message.map {
        Message(
            text = it.text,
            messageChatId = it.messageChatId,
            messageUserId = it.messageUserId
        )
    }
}

fun grpcToMessageUnMono(message: Services.MessageDescription): Message {
    return message.let {
        Message(
            id =ObjectId.get().toString(),
            text = it.text,
            messageChatId = it.messageChatId,
            messageUserId = it.messageUserId
        )
    }
}
fun grpcToMessageUnMono(message: Services.MessageResponse): Message {
    return message.let {
        Message(
            id = it.id,
            text = it.text,
            messageChatId = it.messageChatId,
            messageUserId = it.messageUserId
        )
    }
}
fun grpcResponseToMessageMono(message: Mono<Services.MessageResponse>): Mono<Message> {
    return message.map {
        Message(
            id = it.id,
            text = it.text,
            messageChatId = it.messageChatId,
            messageUserId = it.messageUserId
        )
    }
}
fun messageToGrpc(messages: Flux<Message?>): Flux<Services.MessageResponse>{

    return messages.map {
        Services.MessageResponse
            .newBuilder()
            .apply {
                id = it!!.id
                text = it.text
                dateTime = timestampFromLocalDate(LocalDateTime.parse(it.datetime))
                messageChatId = it.messageChatId
                messageUserId = it.messageUserId
            }
            .build()
    }

}

fun messageToGrpcMono(messages: Mono<Message>): Mono<Services.MessageResponse>{
    return messages.map {
        Services.MessageResponse
            .newBuilder()
            .apply {
                id = it!!.id
                text = it.text
                dateTime = timestampFromLocalDate(LocalDateTime.parse(it.datetime))
                messageChatId = it.messageChatId
                messageUserId = it.messageUserId
            }.build()
    }

}

fun messageToGrpc(it: Message): Services.MessageDescription{
    return Services.MessageDescription
            .newBuilder()
            .apply {
                text = it.text
                messageChatId = it.messageChatId
                messageUserId = it.messageUserId
            }.build()

}


fun updateGrpcToMessage(request: Mono<Services.MessageUpdateRequest>?): Mono<Pair<String, Message>>{
    return request!!.map {
        it!!.messageId.toString() to Message(
            text = it.message.text
        )
    }
}
fun updateMessageToGrpc(message: Message): Mono<Services.MessageResponse>{
    return Services.MessageResponse
        .newBuilder()
        .apply {
            text = message.text
            dateTime = timestampFromLocalDate(LocalDateTime.parse(message.datetime))
        }
        .build()
        .toMono()
}

fun messageToGrpcUnMono(message: Message): Services.MessageResponse{
    return Services.MessageResponse
        .newBuilder()
        .apply {
            id = message.id
            text = message.text
            dateTime = timestampFromLocalDate(LocalDateTime.parse(message.datetime))
            messageChatId = message.messageChatId
            messageUserId = message.messageUserId
        }
        .build()
}

fun updateMessageToGrpcMono(message: Mono<Message>): Mono<Services.MessageResponse>{
    return message.map {
        Services.MessageResponse
            .newBuilder()
            .apply {
                text = it.text
                dateTime = timestampFromLocalDate(LocalDateTime.parse(it.datetime))
            }
            .build()
    }
}

fun updateMessageToGrpcUnMono(it: Message): Services.MessageResponse{
    return Services.MessageResponse
            .newBuilder()
            .apply {
                id = it.id
                text = it.text
                dateTime = timestampFromLocalDate(LocalDateTime.parse(it.datetime))
                messageChatId = it.messageChatId
                messageUserId = it.messageUserId
            }
            .build()

}