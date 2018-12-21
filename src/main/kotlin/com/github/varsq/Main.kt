package com.github.varsq

import kotlinx.coroutines.*
import java.util.*

fun main(args: Array<String>) {

    val login: String
    val password: String

    if (args.size == 2) {
        login = args[0]
        password = args[1]
    } else {
        //Use properties file
        val properties = Properties()
        properties.load(Authentification::class.java.getResourceAsStream("/config.properties"))
        login = properties.getProperty("login")
        password = properties.getProperty("password")
    }
    val auth = Authentification(login, password)
    runBlocking {
        val api = TgvMaxApi(auth.run())
        api.confirmTravels()
    }
}
