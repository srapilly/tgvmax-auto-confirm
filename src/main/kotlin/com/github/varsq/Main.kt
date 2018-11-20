package com.github.varsq

import kotlinx.coroutines.*

fun main(args: Array<String>) {

    if (System.console() != null) {
        val login = System.console().readLine("login : ")
        val password = System.console().readPassword("password : ").joinToString("")
        requireNotNull(login)
        requireNotNull(password)

        val auth = Authentification(login, password)
        runBlocking {
            val api = TgvMaxApi(auth.run())
            api.confirmTravels()
        }
    }
}
