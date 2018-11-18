package com.github.varsq

import kotlinx.coroutines.*

fun main(args: Array<String>) {
    runBlocking {
        val auth = Authentification(args[0], args[1])
        val api = TgvMaxApi(auth.run())
        api.confirmTravels()
    }
}