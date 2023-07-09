package com.example.trenningWebflex

import com.example.trenningWebflex.model.Aircraft
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisOperations
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux

@EnableScheduling
@Component
class PlaneFinderPoller (
    private val connectionFactory: RedisConnectionFactory,
    private val redisOperations: RedisOperations<String, Aircraft>
){
    private val client = WebClient.create("http://192.168.0.121:6379/aircraft")

    @Scheduled(fixedRate = 1000)
    private fun pollPlanes() {
        connectionFactory.connection.serverCommands().flushDb()

        client.get()
            .retrieve()
            .bodyToFlux<Aircraft>()
            .filter { !it.reg.isNullOrEmpty() }
            .toStream()
            .forEach { redisOperations.opsForValue()[it.reg!!] = it }

        redisOperations.opsForValue()
            .operations
            .keys("*")
            ?.forEach { println(redisOperations.opsForValue()[it]) }
    }
}

