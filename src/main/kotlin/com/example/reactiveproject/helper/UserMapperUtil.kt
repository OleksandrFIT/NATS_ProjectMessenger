package com.example.reactiveproject.helper

import com.example.reactiveproject.Services
import com.example.reactiveproject.Services.UserDescription
import com.example.reactiveproject.model.Chat
import com.example.reactiveproject.model.Message
import com.example.reactiveproject.model.User
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.LocalDateTime

//for method find by userName
fun userNameToGrpc(grpcId: Mono<Services.name>): Mono<String>{
    return grpcId.map{
        it.name.toString()
    }
}
//for method find by phoneNumber
fun phoneNumberToGrpc(grpcPhoneNumber: Mono<Services.phoneNumber>): Mono<String>{
    return grpcPhoneNumber.map{
        it.phoneNumber.toString()
    }
}

fun userToGrpcMono(user: User): Mono<Services.UserResponse>{
    var chatList = mutableListOf<Chat>()
    for (i in user.chat){
        chatList.add(i)
    }
    var messageList = mutableListOf<Message>()
    for (i in user.message){
        messageList.add(i)
    }

    val grpcUser = Services.UserResponse
        .newBuilder()
        .apply {
            id = user.id
            bio = user.bio
            name = user.name
            phoneNumber = user.phoneNumber
            messageList = messageList
            chatList = chatList
        }
        .build()
        .toMono()

    return grpcUser
}
fun userToGrpc(user: Mono<User?>): Mono<Services.UserResponse>{

    return user.map {
       Services.UserResponse
           .newBuilder()
           .apply {
               id = it!!.id
               name = it.name
               phoneNumber = it.phoneNumber
               bio = it.bio
               addAllMessage(it.message.map { messageToGrpc(it) })
               addAllChat(it.chat.map { chatToGrpcDescription(it) })
           }
           .build()
    }
}

fun userToGrpcUnMonoResponse(it: User?): Services.UserResponse{
    return Services.UserResponse
            .newBuilder()
            .apply {
                id = it!!.id
                name = it.name
                phoneNumber = it.phoneNumber
                bio = it.bio
                addAllMessage(it.message.map { messageToGrpc(it) })
                addAllChat(it.chat.map { chatToGrpcDescription(it) })
            }
            .build()
}
fun userToGrpcForCreate(user: Mono<User>): Mono<Services.UserResponse> {

    return user.map {
        Services.UserResponse
            .newBuilder()
            .apply {
                id = it!!.id
                name = it.name
                phoneNumber = it.phoneNumber
                bio = it.bio
                addAllMessage(it.message.map { messageToGrpc(it) })
                addAllChat(it.chat.map { chatToGrpcDescription(it) })
            }
            .build()
    }
}
fun userToGrpcDescription(user: User): Services.UserDescription{
    var chatList = mutableListOf<Chat>()
    for (i in user.chat){
        chatList.add(i)
    }
    var messageList = mutableListOf<Message>()
    for (i in user.message){
        messageList.add(i)
    }

    val grpcUser = Services.UserDescription
        .newBuilder()
        .apply {
            bio = user.bio
            name = user.name
            phoneNumber = user.phoneNumber
            messageList = messageList
            chatList = chatList
        }
        .build()


    return grpcUser
}


fun grpcToUser(grpcUser: Mono<Services.UserDescription>): Mono<User> {

    return grpcUser.map {
            User(
                name = it!!.name,
                phoneNumber = it.name,
                bio = it.bio,
                chat = it.chatList.map {
                    grpcToChatUnMono(it)
                },
                message = it.messageList.map {
                    grpcToMessageUnMono(it)
                }
            )
        }
}
fun grpcResponseToUser(grpcUser: Mono<Services.UserResponse>): Mono<User> {

    return grpcUser.map {
        User(
            id = it.id,
            name = it.name,
            phoneNumber = it.name,
            bio = it.bio,
            chat = it.chatList.map {
                grpcToChatUnMono(it)
            },
            message = it.messageList.map {
                grpcToMessageUnMono(it)
            }
        )
    }
}
fun updateGrpcToUser(request: Mono<Services.UserUpdateRequest>?): Mono<Pair<String, User>>{
    return request!!.map {
        it!!.userId.toString() to User(
            id = it.userId.toString(),
            it.user.name,
            it.user.phoneNumber,
            it.user.bio,
            it.user.chatList.map {
                grpcToChatUnMono(it)
            },
            it.user.messageList.map {
                grpcToMessageUnMono(it)
            }
        )
    }
}

fun updateUserToGrpc(user: Mono<User>): Mono<Services.UserResponse>{
    return user.map {
        Services.UserResponse
            .newBuilder()
            .apply {
                id = it.id
                bio = it.bio
                name = it.name
                phoneNumber = it.phoneNumber
                addAllMessage(it.message.map { messageToGrpc(it) })
                addAllChat(it.chat.map { chatToGrpcDescription(it) })
            }.build()
    }
}

fun updateUserToGrpcUnMono(it: User): Services.UserResponse{
    return Services.UserResponse
            .newBuilder()
            .apply {
                id = it.id
                bio = it.bio
                name = it.name
                phoneNumber = it.phoneNumber
                addAllMessage(it.message.map { messageToGrpc(it) })
                addAllChat(it.chat.map { chatToGrpcDescription(it) })
            }.build()

}