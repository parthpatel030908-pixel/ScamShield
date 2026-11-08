
package com.example.mobilesecuritylab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val SERVER_IP = "192.168.68.128"
private const val SERVER_PORT = 8000

private const val HEALTH_URL =
    "http://$SERVER_IP:$SERVER_PORT/health"

private const val SCAN_URL =
    "http://$SERVER_IP:$SERVER_PORT/check-url"

private val CyberBackground = Color(0xFF08111F)
private val CyberGreen = Color(0xFF36E6A0)
private val CyberCard = Color(0xFF142238)
private val CyberRed = Color(0xFFFF6B6B)
private val CyberAmber = Color(0xFFFFC857)

data class HealthResult(
    val message: String,
    val httpCode: Int?,
    val responseBody: String,
    val responseTimeMs: Long,
    val online: Boolean,
    val checkedAt: String = SimpleDateFormat(
        "HH:mm:ss",
        Locale.getDefault()
    ).format(Date())
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = if (
            intent?.action == Intent.ACTION_SEND &&
            intent.type == "text/plain"
        ) {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else null

        setContent {
            MaterialTheme {
                CyberLabApp(sharedText = sharedText)
            }
        }
    }
}

@Composable
fun CyberLabApp(sharedText: String? = null) {

    var status by remember {
        mutableStateOf("Ready — no connection tested yet")
    }
    var isChecking by remember { mutableStateOf(false) }
    var lastChecked by remember { mutableStateOf("Never") }
    var isOnline by remember { mutableStateOf<Boolean?>(null) }

    var selectedModule by remember { mutableStateOf<String?>(null) }
    var healthResult by remember { mutableStateOf<HealthResult?>(null) }

    val healthHistory = remember {
        mutableStateListOf<HealthResult>()
    }

    val scope = rememberCoroutineScope()

    fun checkServer() {
        if (isChecking) return

        scope.launch {
            isChecking = true
            status = "Checking Kali server..."

            val result = withContext(Dispatchers.IO) {
                checkCyberLabServer()
            }

            healthResult = result
            healthHistory.add(0, result)

            while (healthHistory.size > 50) {
                healthHistory.removeAt(healthHistory.lastIndex)
            }

            status = result.message
            isOnline = result.online
            lastChecked = result.checkedAt
            isChecking = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CyberBackground
    ) {
        when (selectedModule) {

            null -> DashboardScreen(
                status,
                isChecking,
                lastChecked,
                isOnline,
                { checkServer() },
                { selectedModule = it },
                sharedText
            )

            "Network Tools" -> NetworkToolsScreen(
                healthResult,
                isChecking,
                { checkServer() },
                { selectedModule = null }
            )

            "Network Diagnostics" -> NetworkDiagnosticsScreen(
                healthHistory,
                isChecking,
                { checkServer() },
                { selectedModule = null }
            )

            "Lab Activity Log" -> LabActivityLogScreen(
                healthHistory,
                { healthHistory.clear() },
                { selectedModule = null }
            )

            "Link Scanner" -> LinkScannerScreen(
                { selectedModule = null },
                sharedText
            )

            else -> LearningModuleScreen(
                selectedModule!!,
                { selectedModule = null }
            )
        }
    }
}

@Composable
fun DashboardScreen(
    status: String,
    isChecking: Boolean,
    lastChecked: String,
    isOnline: Boolean?,
    onCheckLab: () -> Unit,
    onOpenModule: (String) -> Unit,
    sharedText: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        if (!sharedText.isNullOrBlank()) {
            Text(
                "SHARED LINK:\n$sharedText",
                color = CyberGreen
            )
        }

        Text(
            "SCAMSHIELD",
            color = CyberGreen,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Scam Detection & Cybersecurity Lab",
            color = Color.White,
            fontSize = 16.sp
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCard, RoundedCornerShape(18.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "LAB STATUS",
                color = CyberGreen,
                fontWeight = FontWeight.Bold
            )

            Text(status, color = Color.White)

            Text(
                when (isOnline) {
                    true -> "● ONLINE"
                    false -> "● OFFLINE"
                    null -> "● NOT TESTED"
                },
                color = when (isOnline) {
                    true -> CyberGreen
                    false -> CyberRed
                    null -> Color.LightGray
                },
                fontWeight = FontWeight.Bold
            )

            Text(
                "Last checked: $lastChecked",
                color = Color.LightGray,
                fontSize = 12.sp
            )

            Button(
                onClick = onCheckLab,
                enabled = !isChecking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isChecking) "Checking..." else "Check Lab")
            }
        }

        Text(
            "LEARNING MODULES",
            color = CyberGreen,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        ModuleCard("01", "Networking Basics", "IP addresses, ports, DNS and HTTP") {
            onOpenModule("Networking Basics")
        }

        ModuleCard("02", "Android Security", "Permissions and app safety") {
            onOpenModule("Android Security")
        }

        ModuleCard("03", "Kali Linux Lab", "Linux commands and lab basics") {
            onOpenModule("Kali Linux Lab")
        }

        ModuleCard("04", "Network Tools", "Health check, response time and JSON") {
            onOpenModule("Network Tools")
        }

        ModuleCard("05", "Network Diagnostics", "Connection analysis and recent test history") {
            onOpenModule("Network Diagnostics")
        }

        ModuleCard("06", "Lab Activity Log", "View, filter and clear health-check records") {
            onOpenModule("Lab Activity Log")
        }

        ModuleCard("07", "Link Scanner", "Check URLs using threat intelligence sources") {
            onOpenModule("Link Scanner")
        }

        Text(
            "Educational use • Test only your own lab",
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ModuleCard(
    number: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberCard, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(number, color = CyberGreen, fontSize = 13.sp)

        Text(
            title,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Text(subtitle, color = Color.LightGray, fontSize = 13.sp)

        Text("Open module →", color = CyberGreen, fontSize = 13.sp)
    }
}

@Composable
fun NetworkToolsScreen(
    result: HealthResult?,
    isChecking: Boolean,
    onCheck: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Button(onClick = onBack) {
            Text("← Back to Dashboard")
        }

        Text(
            "NETWORK TOOLS",
            color = CyberGreen,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Target: your own CyberLab health endpoint",
            color = Color.LightGray
        )

        Text(HEALTH_URL, color = Color.White, fontSize = 14.sp)

        Button(
            onClick = onCheck,
            enabled = !isChecking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isChecking) "Checking..." else "Run Health Check")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCard, RoundedCornerShape(16.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("RESULTS", color = CyberGreen, fontWeight = FontWeight.Bold)

            if (result == null) {
                Text("No test run yet. Tap Run Health Check.", color = Color.White)
            } else {
                Text(
                    if (result.online) "● ONLINE" else "● UNAVAILABLE",
                    color = if (result.online) CyberGreen else CyberRed,
                    fontWeight = FontWeight.Bold
                )

                Text("HTTP status: ${result.httpCode ?: "No response"}", color = Color.White)
                Text("Response time: ${result.responseTimeMs} ms", color = Color.White)

                Text("Server response:", color = CyberGreen, fontWeight = FontWeight.Bold)

                Text(
                    result.responseBody.ifBlank { "No response body available." },
                    color = Color.White,
                    fontSize = 13.sp
                )

                Text(result.message, color = Color.LightGray, fontSize = 13.sp)
            }
        }

        Text(
            "This tool checks only the configured lab health endpoint.",
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun NetworkDiagnosticsScreen(
    history: List<HealthResult>,
    isChecking: Boolean,
    onCheck: () -> Unit,
    onBack: () -> Unit
) {
    val latest = history.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Button(onClick = onBack) {
            Text("← Back to Dashboard")
        }

        Text(
            "NETWORK DIAGNOSTICS",
            color = CyberGreen,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Check the connection to your own Kali lab server.",
            color = Color.LightGray
        )

        Button(
            onClick = onCheck,
            enabled = !isChecking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isChecking) "Testing..." else "Run Diagnostic Test")
        }

        Text("LATEST RESULT", color = CyberGreen, fontWeight = FontWeight.Bold)

        if (latest == null) {
            Text("No tests yet. Run a diagnostic test to begin.", color = Color.White)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCard, RoundedCornerShape(16.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    if (latest.online) "● SERVER ONLINE" else "● SERVER UNAVAILABLE",
                    color = if (latest.online) CyberGreen else CyberRed,
                    fontWeight = FontWeight.Bold
                )

                Text("Checked at: ${latest.checkedAt}", color = Color.White)
                Text("HTTP status: ${latest.httpCode ?: "No response"}", color = Color.White)
                Text("Response time: ${latest.responseTimeMs} ms", color = Color.White)

                Text("What this means", color = CyberGreen, fontWeight = FontWeight.Bold)

                Text(
                    when {
                        latest.httpCode == null ->
                            "No HTTP response was received. Check that the Kali server is running and the emulator can reach it."

                        latest.httpCode == 200 && latest.online ->
                            "HTTP 200 means the request succeeded. The server returned an online status."

                        latest.httpCode in 200..299 ->
                            "The server returned a successful HTTP status, but its response did not contain the expected online status."

                        else ->
                            "The server responded with a non-success HTTP status. Check the status code and server configuration."
                    },
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }
        }

        Text("RECENT TEST HISTORY", color = CyberGreen, fontWeight = FontWeight.Bold)

        if (history.isEmpty()) {
            Text("Your recent tests will appear here.", color = Color.LightGray)
        } else {
            history.forEachIndexed { index, item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberCard, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Test ${index + 1} • ${item.checkedAt}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        if (item.online) "ONLINE" else "UNAVAILABLE",
                        color = if (item.online) CyberGreen else CyberRed
                    )

                    Text(
                        "HTTP ${item.httpCode ?: "N/A"} • ${item.responseTimeMs} ms",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Text(
            "History lasts only while this app session is active.",
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun LabActivityLogScreen(
    history: List<HealthResult>,
    onClearHistory: () -> Unit,
    onBack: () -> Unit
) {
    var filter by remember { mutableStateOf("ALL") }

    val filteredHistory = history.filter { item ->
        when (filter) {
            "SUCCESS" -> item.online
            "FAILED" -> !item.online
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = onBack) {
            Text("← Back to Dashboard")
        }

        Text(
            "LAB ACTIVITY LOG",
            color = CyberGreen,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Automatically records completed health checks.",
            color = Color.LightGray
        )

        Text("Total records: ${history.size}", color = Color.White, fontWeight = FontWeight.Bold)

        Text("FILTER RECORDS", color = CyberGreen, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "SUCCESS", "FAILED").forEach { option ->
                Button(
                    onClick = { filter = option },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(option)
                }
            }
        }

        if (filteredHistory.isEmpty()) {
            Text(
                if (history.isEmpty()) {
                    "No activity yet. Open Network Tools and run a health check."
                } else {
                    "No records match this filter."
                },
                color = Color.LightGray
            )
        } else {
            filteredHistory.forEachIndexed { index, item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberCard, RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Activity ${index + 1} • ${item.checkedAt}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        if (item.online) "SUCCESS — Server online"
                        else "FAILED — Server unavailable",
                        color = if (item.online) CyberGreen else CyberRed,
                        fontWeight = FontWeight.Bold
                    )

                    Text("HTTP status: ${item.httpCode ?: "No response"}", color = Color.White)
                    Text("Response time: ${item.responseTimeMs} ms", color = Color.White)
                    Text(item.message, color = Color.LightGray, fontSize = 13.sp)
                }
            }
        }

        Button(onClick = onClearHistory) {
            Text("Clear Activity History")
        }
    }
}

private fun checkCyberLabServer(): HealthResult {
    val startTime = System.currentTimeMillis()
    var connection: HttpURLConnection? = null

    return try {
        connection = URL(HEALTH_URL).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        val code = connection.responseCode

        val stream = if (code in 200..399) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val body = stream?.bufferedReader()?.use { it.readText() } ?: ""

        val online = code == 200 &&
                body.contains("online", ignoreCase = true)

        HealthResult(
            message = if (online) {
                "Connected — ScamShield server online"
            } else {
                "Server responded with HTTP $code"
            },
            httpCode = code,
            responseBody = body,
            responseTimeMs = System.currentTimeMillis() - startTime,
            online = online
        )
    } catch (e: Exception) {
        HealthResult(
            message = "Connection failed: ${e.message ?: "Unknown error"}",
            httpCode = null,
            responseBody = "",
            responseTimeMs = System.currentTimeMillis() - startTime,
            online = false
        )
    } finally {
        connection?.disconnect()
    }
}

@Composable
fun LearningModuleScreen(
    module: String,
    onBack: () -> Unit
) {
    val details = when (module) {
        "Networking Basics" -> """
            • IP addresses identify devices on a network.
            • Ports identify services on a device.
            • DNS translates domain names into IP addresses.
            • HTTP is used for web communication.
        """.trimIndent()

        "Android Security" -> """
            • Android apps request permissions for protected features.
            • Install apps only from trusted sources.
            • Review permissions before granting access.
            • Keep Android and apps updated.
        """.trimIndent()

        "Kali Linux Lab" -> """
            • pwd displays your current directory.
            • ls lists files and folders.
            • cd changes directories.
            • Practice commands only in your own lab.
        """.trimIndent()

        else -> "Select a learning module from the dashboard."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Button(onClick = onBack) {
            Text("← Back to Dashboard")
        }

        Text(
            module,
            color = CyberGreen,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            details,
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 25.sp
        )
    }
}

private data class SourceStatus(
    val name: String,
    val checked: Boolean,
    val matched: Boolean
)

private data class ScanDisplayResult(
    val verdict: String,
    val reliability: String,
    val message: String,
    val sources: List<SourceStatus>,
    val warnings: List<String>,
    val httpCode: Int
)

private fun performUrlScan(candidate: String): ScanDisplayResult {
    var connection: HttpURLConnection? = null

    try {
        connection = URL(SCAN_URL).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 10000
        connection.readTimeout = 20000
        connection.doOutput = true

        connection.setRequestProperty(
            "Content-Type",
            "application/json"
        )

        val request = JSONObject()
            .put("url", candidate)
            .toString()

        connection.outputStream.use { output ->
            output.write(request.toByteArray(Charsets.UTF_8))
        }

        val statusCode = connection.responseCode

        val responseStream = if (statusCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val body = responseStream
            ?.bufferedReader()
            ?.use { it.readText() }
            .orEmpty()

        if (body.isBlank()) {
            throw Exception("Empty server response. HTTP $statusCode")
        }

        val json = JSONObject(body)

        val verdict = json.optString("verdict", "UNKNOWN")
        val message = json.optString("message", "No additional details.")

        val sourcesJson = json.optJSONArray("sources") ?: JSONArray()
        val warningsJson = json.optJSONArray("warnings")

        val sources = mutableListOf<SourceStatus>()

        for (i in 0 until sourcesJson.length()) {
            val item = sourcesJson.optJSONObject(i) ?: continue

            val name = item.optString("source", "Unknown source")
            val matched = item.optBoolean("match", false)

            // A returned source entry represents a completed source check
            // only when the backend reports a match field.
            val checked = item.has("match") &&
                    !item.isNull("match")

            sources.add(
                SourceStatus(
                    name = name,
                    checked = checked,
                    matched = matched
                )
            )
        }

        val warnings = mutableListOf<String>()

        if (warningsJson != null) {
            for (i in 0 until warningsJson.length()) {
                warnings.add(warningsJson.optString(i))
            }
        }

        val checkedCount = sources.count { it.checked }

        val reliability = when {
            checkedCount >= 2 && warnings.isEmpty() ->
                "2 SOURCES CHECKED"

            checkedCount > 0 ->
                "PARTIALLY CHECKED"

            else ->
                "CHECK UNAVAILABLE"
        }

        return ScanDisplayResult(
            verdict = verdict,
            reliability = reliability,
            message = message,
            sources = sources,
            warnings = warnings,
            httpCode = statusCode
        )

    } finally {
        connection?.disconnect()
    }
}

@Composable
fun LinkScannerScreen(
    onBack: () -> Unit,
    sharedText: String? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var url by remember {
        mutableStateOf(
            sharedText
                ?.let {
                    Regex("""https?://\S+""")
                        .find(it)?.value
                }
                ?.trimEnd('.', ',', ')', ']', '!')
                ?: ""
        )
    }

    val prefs = remember(context) {
        context.getSharedPreferences(
            "ScamShieldHistory",
            Context.MODE_PRIVATE
        )
    }

    var scanHistory by remember {
        mutableStateOf(
            runCatching {
                val saved = prefs.getString("scan_history", "[]") ?: "[]"
                val array = JSONArray(saved)

                List(array.length()) { index ->
                    array.getString(index)
                }
            }.getOrDefault(emptyList())
        )
    }

    var result by remember { mutableStateOf<ScanDisplayResult?>(null) }
    var resultMessage by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }

    var showClearHistoryDialog by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Button(onClick = onBack) {
            Text("← Back to Dashboard")
        }

        Text(
            "LINK SCANNER",
            color = CyberGreen,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Check suspicious links using URLhaus and Google Safe Browsing.",
            color = Color.LightGray
        )

        OutlinedTextField(
            value = url,
            onValueChange = {
                url = it
                result = null
                resultMessage = ""
            },
            label = { Text("Enter URL") },
            placeholder = { Text("https://example.com") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isChecking
        )

        Button(
            onClick = {
                val candidate = url.trim()

                val parsed = runCatching {
                    URI(candidate)
                }.getOrNull()

                val validUrl = parsed != null &&
                        (
                                parsed.scheme.equals("http", true) ||
                                        parsed.scheme.equals("https", true)
                                ) &&
                        !parsed.host.isNullOrBlank()

                if (!validUrl) {
                    result = null
                    resultMessage =
                        "INVALID URL — Enter a valid HTTP or HTTPS URL."
                } else {
                    scope.launch {
                        isChecking = true
                        result = null
                        resultMessage = "Checking URL with threat intelligence sources..."

                        try {
                            val scan = withContext(Dispatchers.IO) {
                                performUrlScan(candidate)
                            }

                            result = scan

                            resultMessage = when (scan.verdict) {
                                "LIKELY_MALICIOUS" ->
                                    "LIKELY MALICIOUS"

                                "NO_KNOWN_THREAT_FOUND" ->
                                    "NO KNOWN THREAT FOUND"

                                "CHECK_UNAVAILABLE" ->
                                    "CHECK UNAVAILABLE"

                                "INVALID_URL" ->
                                    "INVALID URL"

                                else ->
                                    "UNKNOWN RESULT"
                            }

                            val scannedAt = SimpleDateFormat(
                                "dd MMM yyyy, hh:mm a",
                                Locale.getDefault()
                            ).format(Date())

                            val sourceHistory = buildString {
                                append("URL: $candidate\n")
                                append("Scanned: $scannedAt\n")
                                append("Verdict: ${scan.verdict}\n")
                                append("Reliability: ${scan.reliability}\n")
                                append("Message: ${scan.message}\n")

                                scan.sources.forEach {
                                    append(
                                        "${it.name}: ${
                                            if (it.matched) "THREAT DETECTED"
                                            else if (it.checked) "No known match"
                                            else "Not checked"
                                        }\n"
                                    )
                                }

                                scan.warnings.forEach {
                                    append("Warning: $it\n")
                                }
                            }

                            scanHistory = listOf(sourceHistory) + scanHistory

                        } catch (e: Exception) {
                            result = null
                            resultMessage =
                                "CHECK FAILED\n\n${e.message ?: "Unknown connection error"}"

                            val scannedAt = SimpleDateFormat(
                                "dd MMM yyyy, hh:mm a",
                                Locale.getDefault()
                            ).format(Date())

                            scanHistory = listOf(
                                "URL: $candidate\nScanned: $scannedAt\n$resultMessage"
                            ) + scanHistory
                        } finally {
                            isChecking = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isChecking
        ) {
            Text(if (isChecking) "Checking..." else "Check URL")
        }

        if (isChecking) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = CyberGreen
            )
        }

        if (resultMessage.isNotBlank() && !isChecking) {
            val verdictColor = when (result?.verdict) {
                "LIKELY_MALICIOUS" -> CyberRed
                "NO_KNOWN_THREAT_FOUND" -> CyberGreen
                "CHECK_UNAVAILABLE" -> CyberAmber
                else -> CyberAmber
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCard, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    "SCAN RESULT",
                    color = CyberGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    resultMessage,
                    color = verdictColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                result?.let { scan ->

                    HorizontalDivider(color = Color.DarkGray)

                    Text(
                        "SCAN RELIABILITY",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        scan.reliability,
                        color = when (scan.reliability) {
                            "2 SOURCES CHECKED" -> CyberGreen
                            "PARTIALLY CHECKED" -> CyberAmber
                            else -> CyberRed
                        },
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider(color = Color.DarkGray)

                    Text(
                        "SOURCE STATUS",
                        color = CyberGreen,
                        fontWeight = FontWeight.Bold
                    )

                    scan.sources.forEach { source ->

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                source.name,
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                                fontSize = 13.sp
                            )

                            Text(
                                when {
                                    source.matched -> "THREAT FOUND"
                                    source.checked -> "NO MATCH"
                                    else -> "NOT CHECKED"
                                },
                                color = when {
                                    source.matched -> CyberRed
                                    source.checked -> CyberGreen
                                    else -> CyberAmber
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    HorizontalDivider(color = Color.DarkGray)

                    Text(
                        scan.message,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )

                    if (scan.verdict == "LIKELY_MALICIOUS") {
                        Text(
                            "Recommendation: Do not open this link.",
                            color = CyberRed,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (scan.verdict == "NO_KNOWN_THREAT_FOUND") {
                        Text(
                            "A negative result does not guarantee that this URL is safe.",
                            color = CyberAmber,
                            fontSize = 13.sp
                        )
                    }

                    if (scan.warnings.isNotEmpty()) {
                        Text(
                            "WARNINGS",
                            color = CyberAmber,
                            fontWeight = FontWeight.Bold
                        )

                        scan.warnings.forEach { warning ->
                            Text(
                                "• $warning",
                                color = CyberAmber,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        "HTTP status: ${scan.httpCode}",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }

                if (result == null) {
                    Text(
                        resultMessage,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Text(
            "SCAN HISTORY",
            color = CyberGreen,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        if (scanHistory.isEmpty()) {
            Text(
                "No scans yet. Checked URLs will appear here.",
                color = Color.LightGray
            )
        } else {
            scanHistory.forEachIndexed { index, record ->

                var expanded by remember(record) {
                    mutableStateOf(false)
                }

                val savedUrl = record
                    .substringAfter("URL: ", "")
                    .substringBefore("\n")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberCard, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        savedUrl,
                        color = CyberGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        if (expanded) "Hide details ▲" else "View details ▼",
                        color = Color.LightGray,
                        modifier = Modifier.clickable {
                            expanded = !expanded
                        }
                    )

                    if (expanded) {

                        Text(
                            record.substringAfter("\n", ""),
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 21.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (savedUrl.isNotBlank()) {
                                        clipboardManager.setText(
                                            AnnotatedString(savedUrl)
                                        )
                                    }
                                }
                            ) {
                                Text("Copy URL")
                            }

                            OutlinedButton(
                                onClick = {
                                    scanHistory =
                                        scanHistory.filterIndexed { itemIndex, _ ->
                                            itemIndex != index
                                        }
                                }
                            ) {
                                Text("Delete", color = CyberRed)
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { showClearHistoryDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear All Scan History", color = CyberRed)
            }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = {
                showClearHistoryDialog = false
            },
            title = {
                Text("Clear scan history?")
            },
            text = {
                Text("This will permanently remove all saved scan records from this app.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scanHistory = emptyList()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear all", color = CyberRed)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(scanHistory) {
        val array = JSONArray()

        scanHistory.forEach { record ->
            array.put(record)
        }

        prefs.edit()
            .putString("scan_history", array.toString())
            .apply()
    }
}
