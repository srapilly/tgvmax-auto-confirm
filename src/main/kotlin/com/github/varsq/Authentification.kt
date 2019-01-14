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

class Authentification(val login: String, val passwd: String, val notification: Notification?) {
    data class ApiAuth(val accountId: String, val apiKey: String, val cookies: String)
    data class State(val value: String, val mac: String, val version: String)

    private val baseUrl = "https://happycard.force.com/"
    private val loginUrl = "${baseUrl}SiteLogin"
    private val homeUrl = "${baseUrl}apex/SiteHome"
    private val oauth2UrlStart = "${baseUrl}services/oauth2/authorize?response_type=code&client_id="
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
        logger.info { "Starting auth" }
        val hiddenField = getHiddenFieldFromLoginPage(getRequest(loginUrl).readText())
        var nextUrl = login(hiddenField)
        getRequest(nextUrl)
        nextUrl = getRedirect(getRequest(homeUrl).readText())
        val landingPage = getRequest(nextUrl).readText()
        val clientID =  getClientId(landingPage)
        val apiKey = getApiKey(landingPage)
        val tgvMaxAuth = getRequest("$oauth2UrlStart$clientID$oauth2UrlEnd").readText()
        val code = getCode(getRedirect(tgvMaxAuth))
        val auth = getRequest("$apiAuthenticateUrl$code", apiKey)
        val accountId = getAccountId(auth.readText())
        logger.info { "Auth done :)" }
        return ApiAuth(accountId, apiKey, cookieToString(client.cookies("https://www.tgvmax.fr")))
    }

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
        logger.debug { "POST : $url" }
        val res = client.submitForm<HttpResponse>(params) {
            url(URL(loginUrl))
            header("Cookie:", cookieToString(client.cookies(baseUrl)))
        }
        val validPassword = client.cookies(baseUrl).any { it.name == "oinfo" }
        require(validPassword) {
            notification?.sendMessage("Incorrect login/password")
            "Incorrect login/password"
        }
        return getRedirect(res.readText())
    }

    private suspend fun getRequest(url: String, apiKey: String = ""): HttpResponse {
        logger.debug { "GET : $url" }
        return client.get<HttpResponse>() {
            url(URL(url))
            header("Cookie:", cookieToString(client.cookies(baseUrl)))
            if (apiKey.isNotEmpty()) { header("X-Hpy-ApiKey", apiKey) }
        }
    }

    private suspend fun cookieFromTgvMax(): List<Cookie> {
        return client.cookies(baseUrl)
    }

    companion object {
        fun getHiddenFieldFromLoginPage(loginPage: String) : State {
            val doc = Jsoup.parse(loginPage)
            val state = doc.getElementById("com.salesforce.visualforce.ViewState").attr("value")
            val stateMac = doc.getElementById("com.salesforce.visualforce.ViewStateMAC").attr("value")
            val stateVersion = doc.getElementById("com.salesforce.visualforce.ViewStateVersion").attr("value")
            requireNotNull(state) { "State not found in hidden field from login page"}
            requireNotNull(stateMac) { "State MAC not found in hidden field from login page"}
            requireNotNull(stateVersion) { "State Version not found in hidden field from login page"}
            return State(state, stateMac, stateVersion)
        }

        private fun getInfoFromPage(page: String, start: String, end: String, toFind: String): String {
            val indexStart = page.indexOf(start) + start.length
            if (indexStart == -1) { throw IllegalStateException("$toFind not found, start index: $start not found")}

            val indexEnd = page.indexOf(end, startIndex = indexStart)
            if (indexEnd == -1) { throw IllegalStateException("$toFind not found, end index $end not found")}
            val info = page.substring(indexStart, indexEnd)
            logger.debug { "$toFind -> $info"}
            return info
        }

        fun getClientId(landingPage: String): String {
            return getInfoFromPage(landingPage, """"global.salesforce.authentication.client.id","value":"""", """"},""", "clientID")
        }

        fun getApiKey(landingPage: String): String {
            return getInfoFromPage(landingPage, "apikey\":\"","\"}}}", "API key")
        }

        fun getCode(url: String): String {
            return getInfoFromPage(url, "sfauthcallback?code=","&state", "Auth Code")
        }

        fun getRedirect(script: String): String {
            return getInfoFromPage(script, "handleRedirect('","');", "Redirect")
        }

        fun getAccountId(json: String): String {
            val parser = Parser()
            val jsonObject = parser.parse(StringBuilder(json)) as JsonObject
            val accountId = jsonObject.string("accountId")
            requireNotNull(accountId) { "Account ID not found" }
            logger.debug { "Account ID -> $accountId" }
            return accountId
        }

        fun cookieToString(cookies: List<Cookie>) : String {
            return cookies.fold("") { acc, cookie -> acc.plus(cookie.name + "=" + cookie.value + ";") }
        }


    }
}