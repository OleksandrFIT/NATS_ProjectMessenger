package com.example.reactiveproject.service.impl

import com.example.reactiveproject.helper.idToGrpc
import com.example.reactiveproject.helper.messageToGrpcUnMono
import com.example.reactiveproject.model.Message
import com.example.reactiveproject.redisService.MessageRedisService
import com.example.reactiveproject.repository.MessageRepository
import com.example.reactiveproject.service.MessageService
import io.nats.client.Connection
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.LocalDateTime
import kotlin.math.log

@Service
class MessageServiceImpl(
    @Autowired
    val messageRepository: MessageRepository,
    val messageRedisService: MessageRedisService
) : MessageService {

    @Autowired
    lateinit var natsConnector: Connection

    override fun sendMessage(message: Message): Mono<Message> {
        val messageSend: Message = Message(
            id = message.id,
            text = message.text,
            messageChatId = message.messageChatId,
            messageUserId = message.messageUserId,
            datetime = LocalDateTime.now().toString()
        )
        return messageRepository
            .save(messageSend)
            .flatMap {
                messageRedisService.sendMessage(it) }
            .doOnSuccess {
            natsConnector.publish("message-event", messageToGrpcUnMono(it).toByteArray())
        }
    }

    override fun deleteMessage(messageId: String): Mono<Void> {
        return messageRepository.deleteById(messageId)
            .then(messageRedisService.deleteMessage(messageId))
    }

    override fun editMessage(messageId: String, message: Message): Mono<Message> {
        return messageRepository.findMessageById(messageId)
            .switchIfEmpty(
                Mono.error(NotFoundException())
            )
            .then(
                messageRepository.save(
                    Message(
                        id = messageId,
                        text = message.text,
                        datetime = LocalDateTime.now().toString(),
                        messageChatId = message.messageChatId,
                        messageUserId = message.messageUserId
                    )
                )
            )
            .flatMap { messageRedisService.editMessage(messageId, it) }
    }

    override fun findMessage(text: String): Flux<Message?> {
        return messageRepository.findMessageByText(text)
            .switchIfEmpty(
                Mono.error(NotFoundException())
            )
    }


}