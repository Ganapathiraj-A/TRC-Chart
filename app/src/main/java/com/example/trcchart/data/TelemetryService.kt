package com.example.trcchart.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object TelemetryService {
    private const val PREFS_NAME = "trc_telemetry_prefs"
    private const val KEY_INSTALL_ID = "key_install_id"
    private const val KEY_CITY = "key_city"
    private const val KEY_REGION = "key_region"
    private const val KEY_COUNTRY = "key_country"
    private const val KEY_IP = "key_ip"
    private const val KEY_TOTAL_ENTRIES = "key_total_entries"
    private const val KEY_USER_NAME = "key_user_name"
    private const val KEY_USER_PHONE = "key_user_phone"

    private const val FIRESTORE_BASE_URL = "https://firestore.googleapis.com/v1/projects/antigravity-app-5c1ff/databases/(default)/documents"

    private var isInitialized = false
    private lateinit var prefs: SharedPreferences
    private val scope = CoroutineScope(Dispatchers.IO)
    private val logBuffer = mutableListOf<String>()

    private fun log(msg: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val entry = "[$time] $msg"
        synchronized(logBuffer) {
            if (logBuffer.size > 100) logBuffer.removeAt(0)
            logBuffer.add(entry)
        }
        android.util.Log.d("TelemetryService", msg)
    }

    fun getDebugLogs(): String {
        synchronized(logBuffer) {
            return if (logBuffer.isEmpty()) "No sync logs recorded yet." else logBuffer.joinToString("\n")
        }
    }

    fun initialize(context: Context) {
        if (isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (!prefs.contains(KEY_INSTALL_ID)) {
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_INSTALL_ID, newId).apply()
        }

        isInitialized = true

        // Trigger background Active User Ping and Historical Entry Sync
        scope.launch {
            pingActiveUser()
            syncHistoricalEntries(context)
        }
    }

    fun getInstallationId(): String {
        return prefs.getString(KEY_INSTALL_ID, "") ?: ""
    }

    private const val KEY_USER_COUNTRY = "key_user_country"
    private const val KEY_USER_STATE = "key_user_state"
    private const val KEY_USER_CITY = "key_user_city"

    fun updateUserProfile(name: String, phone: String, country: String = "", state: String = "", city: String = "") {
        if (!isInitialized) return
        val editor = prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_PHONE, phone)

        if (country.isNotBlank()) editor.putString(KEY_USER_COUNTRY, country)
        if (state.isNotBlank()) editor.putString(KEY_USER_STATE, state)
        if (city.isNotBlank()) editor.putString(KEY_USER_CITY, city)

        editor.apply()

        scope.launch {
            pingActiveUser()
        }
    }

    private const val KEY_CLOUD_SYNC_ENABLED = "key_cloud_sync_enabled"

    fun isCloudSyncEnabled(): Boolean {
        if (!isInitialized) return true
        return prefs.getBoolean(KEY_CLOUD_SYNC_ENABLED, true)
    }

    fun setCloudSyncEnabled(enabled: Boolean) {
        if (!isInitialized) return
        prefs.edit().putBoolean(KEY_CLOUD_SYNC_ENABLED, enabled).apply()
        if (enabled) {
            scope.launch {
                pingActiveUser()
            }
        }
    }

    fun recordEntryLogged(
        entryId: String,
        feelingName: String,
        isGoodKarma: Boolean,
        mindTrapsCount: Int = 0,
        entryTimestamp: Long = System.currentTimeMillis(),
        onResult: ((Boolean) -> Unit)? = null
    ) {
        if (!isInitialized) {
            onResult?.invoke(false)
            return
        }

        val currentTotal = prefs.getInt(KEY_TOTAL_ENTRIES, 0) + 1
        prefs.edit().putInt(KEY_TOTAL_ENTRIES, currentTotal).apply()

        if (!isCloudSyncEnabled()) {
            log("Cloud Sync Disabled (Private Mode active). Skipping entry upload.")
            onResult?.invoke(true)
            return
        }

        scope.launch {
            val installId = getInstallationId()
            val now = entryTimestamp
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))

            val userCountry = prefs.getString(KEY_USER_COUNTRY, "") ?: ""
            val userState = prefs.getString(KEY_USER_STATE, "") ?: ""
            val userCity = prefs.getString(KEY_USER_CITY, "") ?: ""

            val city = if (userCity.isNotBlank()) userCity else (prefs.getString(KEY_CITY, "") ?: "")
            val region = if (userState.isNotBlank()) userState else (prefs.getString(KEY_REGION, "Tamil Nadu") ?: "Tamil Nadu")
            val country = if (userCountry.isNotBlank()) userCountry else (prefs.getString(KEY_COUNTRY, "India") ?: "India")
            val ip = prefs.getString(KEY_IP, "Unknown") ?: "Unknown"
            val userName = prefs.getString(KEY_USER_NAME, "") ?: ""
            val userPhone = prefs.getString(KEY_USER_PHONE, "") ?: ""

            // 1. Post event to Cloud Firestore events collection with document ID = entryId
            val eventPayload = """
                {
                    "fields": {
                        "installationId": { "stringValue": "$installId" },
                        "userName": { "stringValue": "${escapeJson(userName)}" },
                        "userPhone": { "stringValue": "${escapeJson(userPhone)}" },
                        "feeling": { "stringValue": "${escapeJson(feelingName)}" },
                        "timestamp": { "integerValue": "$now" },
                        "date": { "stringValue": "$dateStr" },
                        "isGoodKarma": { "booleanValue": $isGoodKarma },
                        "mindTrapsCount": { "integerValue": "$mindTrapsCount" },
                        "city": { "stringValue": "${escapeJson(city)}" },
                        "region": { "stringValue": "${escapeJson(region)}" },
                        "country": { "stringValue": "${escapeJson(country)}" }
                    }
                }
            """.trimIndent()

            val success = postHttpRequest("$FIRESTORE_BASE_URL/events/$entryId", "PATCH", eventPayload)

            // 2. Update user document in Cloud Firestore
            val userPayload = """
                {
                    "fields": {
                        "installationId": { "stringValue": "$installId" },
                        "userName": { "stringValue": "${escapeJson(userName)}" },
                        "userPhone": { "stringValue": "${escapeJson(userPhone)}" },
                        "lastActive": { "integerValue": "$now" },
                        "lastActiveDate": { "stringValue": "$dateStr" },
                        "totalEntriesLogged": { "integerValue": "$currentTotal" },
                        "city": { "stringValue": "${escapeJson(city)}" },
                        "region": { "stringValue": "${escapeJson(region)}" },
                        "country": { "stringValue": "${escapeJson(country)}" },
                        "ip": { "stringValue": "${escapeJson(ip)}" }
                    }
                }
            """.trimIndent()

            postHttpRequest("$FIRESTORE_BASE_URL/users/$installId", "PATCH", userPayload)

            // 3. Update daily stats document in Cloud Firestore
            val dailyUserPayload = """
                {
                    "fields": {
                        "date": { "stringValue": "$dateStr" },
                        "users": {
                            "mapValue": {
                                "fields": {
                                    "$installId": { "booleanValue": true }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
            postHttpRequest("$FIRESTORE_BASE_URL/daily_stats/$dateStr", "PATCH", dailyUserPayload)

            onResult?.invoke(success)
        }
    }

    private fun pingActiveUser() {
        if (!isCloudSyncEnabled()) {
            log("Cloud Sync Disabled. Skipping ping active user.")
            return
        }
        val installId = getInstallationId()
        if (installId.isBlank()) return

        val now = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
        val currentTotal = prefs.getInt(KEY_TOTAL_ENTRIES, 0)

        val userCountry = prefs.getString(KEY_USER_COUNTRY, "") ?: ""
        val userState = prefs.getString(KEY_USER_STATE, "") ?: ""
        val userCity = prefs.getString(KEY_USER_CITY, "") ?: ""

        val city = if (userCity.isNotBlank()) userCity else (prefs.getString(KEY_CITY, "Unknown") ?: "Unknown")
        val region = if (userState.isNotBlank()) userState else (prefs.getString(KEY_REGION, "Unknown") ?: "Unknown")
        val country = if (userCountry.isNotBlank()) userCountry else (prefs.getString(KEY_COUNTRY, "Unknown") ?: "Unknown")
        val ip = prefs.getString(KEY_IP, "Unknown") ?: "Unknown"
        val userName = prefs.getString(KEY_USER_NAME, "") ?: ""
        val userPhone = prefs.getString(KEY_USER_PHONE, "") ?: ""

        val userPayload = """
            {
                "fields": {
                    "installationId": { "stringValue": "$installId" },
                    "userName": { "stringValue": "${escapeJson(userName)}" },
                    "userPhone": { "stringValue": "${escapeJson(userPhone)}" },
                    "lastActive": { "integerValue": "$now" },
                    "lastActiveDate": { "stringValue": "$dateStr" },
                    "totalEntriesLogged": { "integerValue": "$currentTotal" },
                    "city": { "stringValue": "${escapeJson(city)}" },
                    "region": { "stringValue": "${escapeJson(region)}" },
                    "country": { "stringValue": "${escapeJson(country)}" },
                    "ip": { "stringValue": "${escapeJson(ip)}" }
                }
            }
        """.trimIndent()

        val dailyUserPayload = """
            {
                "fields": {
                    "date": { "stringValue": "$dateStr" },
                    "users": {
                        "mapValue": {
                            "fields": {
                                "$installId": { "booleanValue": true }
                            }
                        }
                    }
                }
            }
        """.trimIndent()

        postHttpRequest("$FIRESTORE_BASE_URL/users/$installId", "PATCH", userPayload)
        postHttpRequest("$FIRESTORE_BASE_URL/daily_stats/$dateStr", "PATCH", dailyUserPayload)
    }

    private fun fetchLocationIfNecessary() {
        if (!isCloudSyncEnabled()) return
        try {
            val url = URL("https://ipapi.co/json/")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == 200) {
                val jsonString = conn.inputStream.bufferedReader().use { it.readText() }
                val json = Json.parseToJsonElement(jsonString).jsonObject

                val city = json["city"]?.jsonPrimitive?.content ?: "Unknown"
                val region = json["region"]?.jsonPrimitive?.content ?: "Unknown"
                val country = json["country_name"]?.jsonPrimitive?.content ?: "Unknown"
                val ip = json["ip"]?.jsonPrimitive?.content ?: "Unknown"

                prefs.edit()
                    .putString(KEY_CITY, city)
                    .putString(KEY_REGION, region)
                    .putString(KEY_COUNTRY, country)
                    .putString(KEY_IP, ip)
                    .apply()
            }
        } catch (e: Exception) {
            // Fallback or offline
        }
    }

    private fun postHttpRequest(urlString: String, method: String, jsonPayload: String): Boolean {
        try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            if (method == "PATCH") {
                conn.requestMethod = "POST"
                conn.setRequestProperty("X-HTTP-Method-Override", "PATCH")
            } else {
                conn.requestMethod = method
            }
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.connectTimeout = 7000
            conn.readTimeout = 7000
            conn.doOutput = true

            conn.outputStream.use { os ->
                val input = jsonPayload.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val responseCode = conn.responseCode
            val endpoint = urlString.substringAfter("documents/")
            val isSuccess = responseCode in 200..299
            log("HTTP $method $endpoint -> Status $responseCode ${if (isSuccess) "SUCCESS" else "FAILED"}")
            if (!isSuccess) {
                val errorStream = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                log("Error response: $errorStream")
            }
            conn.disconnect()
            return isSuccess
        } catch (e: Exception) {
            log("HTTP Error: ${e.javaClass.simpleName} - ${e.message}")
            return false
        }
    }

    private fun escapeJson(value: String): String {
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
    }

    private fun syncHistoricalEntries(context: Context) {
        try {
            val mainPrefs = context.applicationContext.getSharedPreferences("trc_chart_prefs", Context.MODE_PRIVATE)
            val savedUserName = mainPrefs.getString("user_name", "") ?: ""
            val savedUserPhone = mainPrefs.getString("user_phone", "") ?: ""

            if (savedUserName.isNotBlank()) {
                prefs.edit()
                    .putString(KEY_USER_NAME, savedUserName)
                    .putString(KEY_USER_PHONE, savedUserPhone)
                    .apply()
            }

            val entriesJson = mainPrefs.getString("key_entries", null) ?: return
            val jsonElement = Json.parseToJsonElement(entriesJson)
            val jsonArray = jsonElement as? kotlinx.serialization.json.JsonArray ?: return

            val installId = getInstallationId()
            val city = prefs.getString(KEY_CITY, "Unknown") ?: "Unknown"
            val region = prefs.getString(KEY_REGION, "Unknown") ?: "Unknown"
            val country = prefs.getString(KEY_COUNTRY, "Unknown") ?: "Unknown"
            val userName = prefs.getString(KEY_USER_NAME, savedUserName) ?: savedUserName
            val userPhone = prefs.getString(KEY_USER_PHONE, savedUserPhone) ?: savedUserPhone

            for (element in jsonArray) {
                val obj = element.jsonObject
                val id = obj["id"]?.jsonPrimitive?.content ?: continue
                val feeling = obj["feeling"]?.jsonPrimitive?.content ?: "Feeling"
                val dateStr = obj["date"]?.jsonPrimitive?.content ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val timestamp = obj["timestamp"]?.jsonPrimitive?.content?.toLongOrNull() ?: System.currentTimeMillis()
                val isGoodKarma = obj["isGoodKarma"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true

                val isBlame = obj["isBlame"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
                val isComplaint = obj["isComplaint"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
                val isExcuse = obj["isExcuse"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
                val isGossip = obj["isGossip"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
                var mindTrapsCount = 0
                if (isBlame) mindTrapsCount++
                if (isComplaint) mindTrapsCount++
                if (isExcuse) mindTrapsCount++
                if (isGossip) mindTrapsCount++

                val eventPayload = """
                    {
                        "fields": {
                            "installationId": { "stringValue": "$installId" },
                            "userName": { "stringValue": "${escapeJson(userName)}" },
                            "userPhone": { "stringValue": "${escapeJson(userPhone)}" },
                            "timestamp": { "integerValue": "$timestamp" },
                            "date": { "stringValue": "$dateStr" },
                            "feeling": { "stringValue": "${escapeJson(feeling)}" },
                            "isGoodKarma": { "booleanValue": $isGoodKarma },
                            "mindTrapsCount": { "integerValue": "$mindTrapsCount" },
                            "city": { "stringValue": "${escapeJson(city)}" },
                            "region": { "stringValue": "${escapeJson(region)}" },
                            "country": { "stringValue": "${escapeJson(country)}" }
                        }
                    }
                """.trimIndent()

                postHttpRequest("$FIRESTORE_BASE_URL/events/$id", "PATCH", eventPayload)
                postHttpRequest("$FIRESTORE_BASE_URL/daily_stats/$dateStr", "PATCH", """
                    {
                        "fields": {
                            "date": { "stringValue": "$dateStr" },
                            "users": {
                                "mapValue": {
                                    "fields": {
                                        "$installId": { "booleanValue": true }
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
