package com.github.varsq

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import mu.KotlinLogging
import java.net.URL

private val logger = KotlinLogging.logger {}
class Notification(private val token: String, private val roomId: String) {
    private val endpoint = "https://matrix.org/_matrix/client/r0/rooms/$roomId/send/m.room.message"

    private val client = HttpClient(Apache) {
        defaultRequest {
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun sendMessage(message: String) {
        val data = """{"msgtype":"m.text", "body":"$message"}"""
        val res = client.post<HttpResponse>() {
            url(URL(endpoint))
            body = data
        }
        val text = res.readText()
        if (res.status.value != 200) {
            logger.error { "Error sending message to Matrix channel\n $text" }
        }
    }
}