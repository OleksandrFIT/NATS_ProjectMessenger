package com.example.reactiveproject.helper

import com.example.reactiveproject.Services
import com.google.protobuf.Timestamp
import reactor.core.publisher.Mono
import java.time.*



fun idToGrpc(grpcId: Mono<Services.id>): Mono<String>{
    return grpcId.map {
        it.id.toString()
    }
}


fun timestampFromLocalDate(localDate: LocalDateTime): Timestamp {
    return Timestamp.newBuilder()
        .setSeconds(localDate.second.toLong())
        .setNanos(localDate.nano)
        .build()
}

fun timestampToLocalDate(timestamp: Timestamp): LocalDate {
    return LocalDateTime.ofInstant(
        Instant.ofEpochSecond(timestamp.seconds, timestamp.nanos.toLong()),
        ZoneId.of("UTC")
    )
        .toLocalDate()
}