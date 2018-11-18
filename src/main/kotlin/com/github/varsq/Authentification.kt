package com.github.varsq

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.cookies.cookies
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.Cookie
import io.ktor.http.Parameters
import mu.KotlinLogging
import org.jsoup.Jsoup
import java.net.URL

private val logger = KotlinLogging.logger {}

class Authentification(val login: String, val passwd: String) {
    data class ApiAuth(val accountId: String, val apiKey: String, val cookies: String)
    data class State(val value: String, val mac: String, val version: String)

    private val loginUrl = "https://happycard.force.com/SiteLogin"
    private val homeUrl = "https://happycard.force.com/apex/SiteHome"
    private val oauth2UrlStart = "https://happycard.force.com/services/oauth2/authorize?response_type=code&client_id="
    private val oauth2UrlEnd = "&redirect_uri=https://www.tgvmax.fr/sfauthcallback&state=trainline%7Cfr-FR%7C%7Creservation%7Cinitrebon"
    private val apiAuthenticateUrl = "https://www.tgvmax.fr/api/authenticate/token?authorization_code="

    private val client = HttpClient(Apache) {
        followRedirects = true
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        defaultRequest {
            header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:60.0) Gecko/20100101 Firefox/60.0")
        }
    }

    suspend fun run(): ApiAuth {
        val hiddenField = getHiddenFieldFromLoginPage(getRequest(loginUrl).readText())
        var nextUrl = login(hiddenField)
        getRequest(nextUrl)
        nextUrl = getRedirect(getRequest(homeUrl).readText())

        val landingPage = getRequest(nextUrl).readText()
        val clientID =  getClientId(landingPage)
        val apiKey = getApiKey(landingPage)
        logger.info { "clientID is $clientID and apiKey is $apiKey" }

        val tgvMaxAuth = getRequest("$oauth2UrlStart$clientID$oauth2UrlEnd").readText()
        val code = getCode(getRedirect(tgvMaxAuth))
        logger.info { "authorization code for Tgv Max API is $code" }

        val auth = getRequest("$apiAuthenticateUrl$code", apiKey)
        val accountId = getAccountId(auth.readText())
        return ApiAuth(accountId, apiKey, cookieToString(client.cookies("https://www.tgvmax.fr")))
    }

    //login and return the next url
    private suspend fun login(state: State): String {
        val params = Parameters.build {
            append("loginPage:SiteTemplate:formulaire", "loginPage:SiteTemplate:formulaire")
            append("loginPage:SiteTemplate:formulaire:j_id37", "Connexion")
            append("loginPage:SiteTemplate:formulaire:login-field",login)
            append("loginPage:SiteTemplate:formulaire:password-field", passwd)
            append("com.salesforce.visualforce.ViewState",state.value)
            append("com.salesforce.visualforce.ViewStateMAC",state.mac)
            append("com.salesforce.visualforce.ViewStateVersion",state.version)
        }
        val url = loginUrl
        logger.info { "new POST request: $url" }
        val res = client.submitForm<HttpResponse>(params) {
            url(URL(loginUrl))
            header("Cookie:", cookieToString(client.cookies("https://happycard.force.com")))
        }
        return getRedirect(res.readText())
    }

    private suspend fun getRequest(url: String, apiKey: String = ""): HttpResponse {
        logger.info { "new GET request: $url" }
        return client.get<HttpResponse>() {
            url(URL(url))
            header("Cookie:", cookieToString(client.cookies("https://happycard.force.com")))
            if (apiKey.isNotEmpty()) { header("X-Hpy-ApiKey", apiKey) }
        }
    }

    companion object {
        fun getAccountId(json: String): String {
            val parser = Parser()
            val jsonObject = parser.parse(StringBuilder(json)) as JsonObject
            val accessToken = jsonObject.string("accessToken")!!
            val accountId = jsonObject.string("accountId")!!
            logger.info { "accessToken is $accessToken" } //Saved in cookie, just for log
            logger.info { "accountId is $accountId" }
            return accountId
        }

        fun getRedirect(script: String): String {
            val startDelimiter = "handleRedirect('"
            val endDelimiter = "');"
            val result = script.substringAfter(startDelimiter).substringBefore(endDelimiter)
            logger.info { "Redirection URL in script is $result" }
            return result
        }

        // Get code from url
        fun getCode(url: String): String {
            val startDelimiter = "sfauthcallback?code="
            val endDelimiter = "&state"
            return url.substringAfter(startDelimiter).substringBefore(endDelimiter)
        }

        fun getApiKey(landingPage: String): String {
            val startDelimiter = "apikey\":\""
            val endDelimiter = "\"}}}"
            return landingPage.substringAfter(startDelimiter).substringBefore(endDelimiter)
        }

        fun getClientId(landingPage: String): String {
            val startDelimiter = """"global.salesforce.authentication.client.id","value":""""
            val endDelimiter = """"},"""
            return landingPage.substringAfter(startDelimiter).substringBefore(endDelimiter)
        }

        fun getHiddenFieldFromLoginPage(loginPage: String) : State {
            val doc = Jsoup.parse(loginPage)
            val state = doc.getElementById("com.salesforce.visualforce.ViewState").attr("value")
            val stateMac = doc.getElementById("com.salesforce.visualforce.ViewStateMAC").attr("value")
            val stateVersion = doc.getElementById("com.salesforce.visualforce.ViewStateVersion").attr("value")
            return State(state, stateMac, stateVersion)
        }

        fun cookieToString(cookies: List<Cookie>) : String {
            return cookies.fold("") { acc, cookie -> acc.plus(cookie.name + "=" + cookie.value + ";") }
        }
    }
}