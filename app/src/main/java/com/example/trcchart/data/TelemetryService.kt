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

    private const val FIREBASE_DATABASE_URL = "https://trc-chart-analytics-default-rtdb.firebaseio.com"

    private var isInitialized = false
    private lateinit var prefs: SharedPreferences
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initialize(context: Context) {
        if (isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (!prefs.contains(KEY_INSTALL_ID)) {
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_INSTALL_ID, newId).apply()
        }

        isInitialized = true

        // Trigger background IP Geolocation, Active User Ping, and Historical Entry Sync
        scope.launch {
            fetchLocationIfNecessary()
            pingActiveUser()
            syncHistoricalEntries(context)
        }
    }

    fun getInstallationId(): String {
        return prefs.getString(KEY_INSTALL_ID, "") ?: ""
    }

    fun updateUserProfile(name: String, phone: String) {
        if (!isInitialized) return
        prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_PHONE, phone)
            .apply()

        scope.launch {
            pingActiveUser()
        }
    }

    fun recordEntryLogged(feelingName: String, isGoodKarma: Boolean, mindTrapsCount: Int = 0) {
        if (!isInitialized) return

        val currentTotal = prefs.getInt(KEY_TOTAL_ENTRIES, 0) + 1
        prefs.edit().putInt(KEY_TOTAL_ENTRIES, currentTotal).apply()

        scope.launch {
            val installId = getInstallationId()
            val now = System.currentTimeMillis()
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))

            val city = prefs.getString(KEY_CITY, "Unknown") ?: "Unknown"
            val region = prefs.getString(KEY_REGION, "Unknown") ?: "Unknown"
            val country = prefs.getString(KEY_COUNTRY, "Unknown") ?: "Unknown"
            val ip = prefs.getString(KEY_IP, "Unknown") ?: "Unknown"
            val userName = prefs.getString(KEY_USER_NAME, "") ?: ""
            val userPhone = prefs.getString(KEY_USER_PHONE, "") ?: ""

            // 1. Post event to events node
            val eventPayload = """
                {
                    "installationId": "$installId",
                    "userName": "${escapeJson(userName)}",
                    "userPhone": "${escapeJson(userPhone)}",
                    "timestamp": $now,
                    "date": "$dateStr",
                    "feeling": "${escapeJson(feelingName)}",
                    "isGoodKarma": $isGoodKarma,
                    "mindTrapsCount": $mindTrapsCount,
                    "city": "${escapeJson(city)}",
                    "region": "${escapeJson(region)}",
                    "country": "${escapeJson(country)}"
                }
            """.trimIndent()

            postHttpRequest("$FIREBASE_DATABASE_URL/events.json", "POST", eventPayload)

            // 2. Update user profile statistics
            val userPayload = """
                {
                    "installationId": "$installId",
                    "userName": "${escapeJson(userName)}",
                    "userPhone": "${escapeJson(userPhone)}",
                    "lastActive": $now,
                    "lastActiveDate": "$dateStr",
                    "totalEntriesLogged": $currentTotal,
                    "city": "${escapeJson(city)}",
                    "region": "${escapeJson(region)}",
                    "country": "${escapeJson(country)}",
                    "ip": "${escapeJson(ip)}"
                }
            """.trimIndent()

            postHttpRequest("$FIREBASE_DATABASE_URL/users/$installId.json", "PATCH", userPayload)

            // 3. Increment daily stats
            val dailyUserPayload = """true"""
            postHttpRequest("$FIREBASE_DATABASE_URL/daily_stats/$dateStr/users/$installId.json", "PUT", dailyUserPayload)
        }
    }

    private fun pingActiveUser() {
        val installId = getInstallationId()
        if (installId.isBlank()) return

        val now = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
        val currentTotal = prefs.getInt(KEY_TOTAL_ENTRIES, 0)

        val city = prefs.getString(KEY_CITY, "Unknown") ?: "Unknown"
        val region = prefs.getString(KEY_REGION, "Unknown") ?: "Unknown"
        val country = prefs.getString(KEY_COUNTRY, "Unknown") ?: "Unknown"
        val ip = prefs.getString(KEY_IP, "Unknown") ?: "Unknown"
        val userName = prefs.getString(KEY_USER_NAME, "") ?: ""
        val userPhone = prefs.getString(KEY_USER_PHONE, "") ?: ""

        val userPayload = """
            {
                "installationId": "$installId",
                "userName": "${escapeJson(userName)}",
                "userPhone": "${escapeJson(userPhone)}",
                "lastActive": $now,
                "lastActiveDate": "$dateStr",
                "totalEntriesLogged": $currentTotal,
                "city": "${escapeJson(city)}",
                "region": "${escapeJson(region)}",
                "country": "${escapeJson(country)}",
                "ip": "${escapeJson(ip)}"
            }
        """.trimIndent()

        postHttpRequest("$FIREBASE_DATABASE_URL/users/$installId.json", "PATCH", userPayload)
        postHttpRequest("$FIREBASE_DATABASE_URL/daily_stats/$dateStr/users/$installId.json", "PUT", "true")
    }

    private fun fetchLocationIfNecessary() {
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

    private fun postHttpRequest(urlString: String, method: String, jsonPayload: String) {
        try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = method
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.connectTimeout = 7000
            conn.readTimeout = 7000
            conn.doOutput = true

            conn.outputStream.use { os ->
                val input = jsonPayload.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            conn.responseCode // Read response code to execute request
            conn.disconnect()
        } catch (e: Exception) {
            // Ignore background network failure silently
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
                        "installationId": "$installId",
                        "userName": "${escapeJson(userName)}",
                        "userPhone": "${escapeJson(userPhone)}",
                        "timestamp": $timestamp,
                        "date": "$dateStr",
                        "feeling": "${escapeJson(feeling)}",
                        "isGoodKarma": $isGoodKarma,
                        "mindTrapsCount": $mindTrapsCount,
                        "city": "${escapeJson(city)}",
                        "region": "${escapeJson(region)}",
                        "country": "${escapeJson(country)}"
                    }
                """.trimIndent()

                postHttpRequest("$FIREBASE_DATABASE_URL/events/$id.json", "PUT", eventPayload)
                postHttpRequest("$FIREBASE_DATABASE_URL/daily_stats/$dateStr/users/$installId.json", "PUT", "true")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
