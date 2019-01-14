package com.github.varsq

import kotlinx.coroutines.*
import java.util.*

suspend fun main(args: Array<String>) {

    val login: String
    val password: String
    var matrixRoom: String? = null
    var matrixToken: String? = null

    if (args.size == 2) {
        login = args[0]
        password = args[1]
    } else {
        //Use properties file
        val properties = Properties()
        properties.load(Authentification::class.java.getResourceAsStream("/config.properties"))
        login = properties.getProperty("login")
        password = properties.getProperty("password")
        matrixToken = properties.getProperty("matrix_token")
        matrixRoom = properties.getProperty("matrix_room_id")
    }

    var notification: Notification? = null
    if (matrixRoom != null && matrixToken != null) {
        notification = Notification(matrixToken,
            matrixRoom)
    }

    val auth = Authentification(login, password, notification)
    runBlocking {
        val api = TgvMaxApi(auth.run(), notification)
        api.confirmTravels()
    }
}
