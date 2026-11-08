
package com.example.mobilesecuritylab

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

private const val SERVER_BASE_URL =
    "https://scamshield-1-d2jy.onrender.com"

private const val HEALTH_URL = "$SERVER_BASE_URL/health"
private const val SCAN_URL = "$SERVER_BASE_URL/check-url"

private const val HISTORY_PREFS = "ScamShieldHistory"
private const val HISTORY_KEY = "scan_history"
private const val MAX_SCAN_HISTORY = 100

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
        } else {
            null
        }

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
            status = "Connecting to ScamShield cloud server..."

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
            "PROTECTION & LEARNING",
            color = CyberGreen,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        ModuleCard(
            "01",
            "Networking Basics",
            "IP addresses, ports, DNS and HTTP"
        ) {
            onOpenModule("Networking Basics")
        }

        ModuleCard(
            "02",
            "Android Security",
            "Permissions and app safety"
        ) {
            onOpenModule("Android Security")
        }

        ModuleCard(
            "03",
            "Kali Linux Lab",
            "Linux commands and lab basics"
        ) {
            onOpenModule("Kali Linux Lab")
        }

        ModuleCard(
            "04",
            "Network Tools",
            "Health check, response time and JSON"
        ) {
            onOpenModule("Network Tools")
        }

        ModuleCard(
            "05",
            "Network Diagnostics",
            "Connection analysis and recent test history"
        ) {
            onOpenModule("Network Diagnostics")
        }

        ModuleCard(
            "06",
            "Lab Activity Log",
            "View, filter and clear health-check records"
        ) {
            onOpenModule("Lab Activity Log")
        }

        ModuleCard(
            "07",
            "Link Scanner",
            "Check URLs using threat intelligence sources"
        ) {
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
            "Target: ScamShield cloud health endpoint",
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
                Text(
                    "No test run yet. Tap Run Health Check.",
                    color = Color.White
                )
            } else {
                Text(
                    if (result.online) "● ONLINE" else "● UNAVAILABLE",
                    color = if (result.online) CyberGreen else CyberRed,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "HTTP status: ${result.httpCode ?: "No response"}",
                    color = Color.White
                )

                Text(
                    "Response time: ${result.responseTimeMs} ms",
                    color = Color.White
                )

                Text(
                    "Server response:",
                    color = CyberGreen,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    result.responseBody.ifBlank {
                        "No response body available."
                    },
                    color = Color.White,
                    fontSize = 13.sp
                )

                Text(
                    result.message,
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            }
        }

        Text(
            "This tool checks the configured ScamShield cloud health endpoint.",
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
            "Check the connection to your ScamShield cloud server.",
            color = Color.LightGray
        )

        Button(
            onClick = onCheck,
            enabled = !isChecking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isChecking) "Testing..." else "Run Diagnostic Test")
        }

        Text(
            "LATEST RESULT",
            color = CyberGreen,
            fontWeight = FontWeight.Bold
        )

        if (latest == null) {
            Text(
                "No tests yet. Run a diagnostic test to begin.",
                color = Color.White
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCard, RoundedCornerShape(16.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    if (latest.online) "● SERVER ONLINE"
                    else "● SERVER UNAVAILABLE",
                    color = if (latest.online) CyberGreen else CyberRed,
                    fontWeight = FontWeight.Bold
                )

                Text("Checked at: ${latest.checkedAt}", color = Color.White)

                Text(
                    "HTTP status: ${latest.httpCode ?: "No response"}",
                    color = Color.White
                )

                Text(
                    "Response time: ${latest.responseTimeMs} ms",
                    color = Color.White
                )

                Text(
                    "What this means",
                    color = CyberGreen,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    when {
                        latest.httpCode == null ->
                            "No HTTP response was received. Check your internet connection and the cloud server status."

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

        Text(
            "RECENT TEST HISTORY",
            color = CyberGreen,
            fontWeight = FontWeight.Bold
        )

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

        Text(
            "Total records: ${history.size}",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

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

                    Text(
                        "HTTP status: ${item.httpCode ?: "No response"}",
                        color = Color.White
                    )

                    Text(
                        "Response time: ${item.responseTimeMs} ms",
                        color = Color.White
                    )

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
        connection.connectTimeout = 30000
        connection.readTimeout = 60000
        connection.useCaches = false

        val code = connection.responseCode

        val stream = if (code in 200..399) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val body = stream?.bufferedReader()?.use {
            it.readText()
        } ?: ""

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
            • HTTPS protects communication using encryption.
        """.trimIndent()

        "Android Security" -> """
            • Android apps request permissions for protected features.
            • Install apps only from trusted sources.
            • Review permissions before granting access.
            • Keep Android and apps updated.
            • Avoid sharing sensitive information with unknown apps.
        """.trimIndent()

        "Kali Linux Lab" -> """
            • pwd displays your current directory.
            • ls lists files and folders.
            • cd changes directories.
            • mkdir creates a directory.
            • Practice commands only in your own lab.
        """.trimIndent()

        "Network Tools" -> """
            • HTTP status codes describe server responses.
            • Response time measures how long a request takes.
            • JSON is a common format for API responses.
            • Use health endpoints to check your own services.
        """.trimIndent()

        "Network Diagnostics" -> """
            • Check whether a server responds.
            • Compare HTTP status codes.
            • Measure response time.
            • Review recent diagnostic results.
        """.trimIndent()

        "Lab Activity Log" -> """
            • Activity logs record completed tests.
            • Successful and failed checks can be filtered.
            • Review timestamps to understand test history.
            • Clear records when they are no longer needed.
        """.trimIndent()

        "Link Scanner" -> """
            • URL scanners compare links against threat intelligence.
            • URLhaus tracks known malicious URLs.
            • Google Safe Browsing checks known unsafe resources.
            • A database match is a warning that a URL may be dangerous.
            • No match does not prove that a URL is safe.
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

// SHAREABLE SCAN REPORT

private fun shareScanReport(context: Context, report: String) {

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "ScamShield Scan Report")
        putExtra(Intent.EXTRA_TEXT, report)
    }

    val chooser = Intent.createChooser(
        sendIntent,
        "Share ScamShield Scan Report"
    )

    context.startActivity(chooser)
}

// PDF REPORT GENERATOR

private fun saveScanReportPdf(
    context: Context,
    uri: Uri,
    report: String
) {
    val document = PdfDocument()

    val paint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 11f
        typeface = Typeface.create(
            Typeface.DEFAULT,
            Typeface.NORMAL
        )
        isAntiAlias = true
    }

    val titlePaint = Paint(paint).apply {
        textSize = 17f
        typeface = Typeface.create(
            Typeface.DEFAULT,
            Typeface.BOLD
        )
    }

    val pageWidth = 595
    val pageHeight = 842
    val margin = 45f
    val lineHeight = 18f
    val maxWidth = pageWidth - margin * 2

    var pageNumber = 1
    var y = 55f

    var page = document.startPage(
        PdfDocument.PageInfo.Builder(
            pageWidth,
            pageHeight,
            pageNumber
        ).create()
    )

    var canvas = page.canvas

    canvas.drawText(
        "SCAMSHIELD - SCAN REPORT",
        margin,
        y,
        titlePaint
    )

    y += 35f

    try {
        report.lines().forEach { originalLine ->

            val words = originalLine.split(" ")
            var currentLine = ""
            val linesToDraw = mutableListOf<String>()

            words.forEach { word ->

                val testLine = if (currentLine.isEmpty()) {
                    word
                } else {
                    "$currentLine $word"
                }

                if (paint.measureText(testLine) > maxWidth) {
                    if (currentLine.isNotEmpty()) {
                        linesToDraw.add(currentLine)
                    }
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }

            if (currentLine.isNotEmpty()) {
                linesToDraw.add(currentLine)
            }

            if (linesToDraw.isEmpty()) {
                linesToDraw.add("")
            }

            linesToDraw.forEach { line ->

                if (y > pageHeight - 55f) {

                    document.finishPage(page)

                    pageNumber++

                    page = document.startPage(
                        PdfDocument.PageInfo.Builder(
                            pageWidth,
                            pageHeight,
                            pageNumber
                        ).create()
                    )

                    canvas = page.canvas
                    y = 55f
                }

                canvas.drawText(
                    line,
                    margin,
                    y,
                    paint
                )

                y += lineHeight
            }
        }

        document.finishPage(page)

        context.contentResolver.openOutputStream(uri)?.use {
            document.writeTo(it)
        } ?: throw Exception("Unable to open the selected file.")

    } finally {
        document.close()
    }
}

// SCANNER DATA MODELS

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

// BACKEND SCAN REQUEST AND RESPONSE HANDLING

private fun performUrlScan(candidate: String): ScanDisplayResult {

    var connection: HttpURLConnection? = null

    try {
        connection = URL(SCAN_URL).openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.connectTimeout = 30000
        connection.readTimeout = 60000
        connection.doOutput = true
        connection.useCaches = false

        connection.setRequestProperty(
            "Content-Type",
            "application/json; charset=utf-8"
        )

        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        val request = JSONObject()
            .put("url", candidate)
            .toString()

        connection.outputStream.use { output ->
            output.write(request.toByteArray(Charsets.UTF_8))
            output.flush()
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
            throw Exception(
                "The server returned an empty response. HTTP $statusCode"
            )
        }

        val json = try {
            JSONObject(body)
        } catch (e: Exception) {
            throw Exception(
                "The server response was not valid JSON. HTTP $statusCode"
            )
        }

        val backendVerdict = json.optString(
            "verdict",
            "UNKNOWN"
        )

        val message = json.optString(
            "message",
            "No additional details."
        )

        val sourcesJson = json.optJSONArray("sources") ?: JSONArray()
        val warningsJson = json.optJSONArray("warnings")

        val sources = mutableListOf<SourceStatus>()

        for (i in 0 until sourcesJson.length()) {

            val item = sourcesJson.optJSONObject(i) ?: continue

            val name = item.optString(
                "source",
                "Unknown source"
            )

            val matchValue = item.opt("match")

            val hasBooleanMatch = matchValue is Boolean
            val matched = matchValue == true

            val checked = statusCode in 200..299 &&
                    hasBooleanMatch

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
                val warning = warningsJson.optString(i)

                if (warning.isNotBlank()) {
                    warnings.add(warning)
                }
            }
        }

        if (statusCode !in 200..299) {
            warnings.add(
                "The backend returned HTTP $statusCode. Source results are not counted as verified."
            )
        }

        val expectedSources = listOf(
            "URLhaus",
            "Google Safe Browsing"
        )

        val checkedCount = expectedSources.count { expectedName ->
            sources.any { source ->
                source.name.equals(expectedName, ignoreCase = true) &&
                        source.checked
            }
        }

        val reliability = when {
            checkedCount == 2 && warnings.isEmpty() ->
                "2 SOURCES CHECKED"

            checkedCount == 1 ->
                "PARTIALLY CHECKED — 1 SOURCE"

            checkedCount > 0 ->
                "PARTIALLY CHECKED"

            else ->
                "CHECK UNAVAILABLE"
        }

        val finalVerdict = if (
            sources.any { it.checked && it.matched }
        ) {
            "LIKELY_MALICIOUS"
        } else {
            backendVerdict
        }

        return ScanDisplayResult(
            verdict = finalVerdict,
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

// REPORT TEXT BUILDER

private fun createScanReport(
    url: String,
    scanTime: String,
    scan: ScanDisplayResult
): String {

    return buildString {

        appendLine("SCAMSHIELD - SCAN REPORT")
        appendLine("========================")
        appendLine()

        appendLine("URL: $url")
        appendLine("Scan time: $scanTime")
        appendLine("Verdict: ${scan.verdict}")
        appendLine("Reliability: ${scan.reliability}")
        appendLine("HTTP status: ${scan.httpCode}")

        appendLine()
        appendLine("THREAT INTELLIGENCE EVIDENCE")
        appendLine("----------------------------")

        scan.sources.forEach { source ->

            val sourceResult = when {
                source.checked && source.matched ->
                    "THREAT DETECTED"

                source.checked ->
                    "NO KNOWN MATCH"

                else ->
                    "NOT CHECKED"
            }

            appendLine("${source.name}: $sourceResult")
        }

        appendLine()
        appendLine("EXPLANATION")
        appendLine("-----------")
        appendLine(scan.message)

        if (scan.warnings.isNotEmpty()) {

            appendLine()
            appendLine("WARNINGS")
            appendLine("--------")

            scan.warnings.forEach { warning ->
                appendLine("- $warning")
            }
        }

        appendLine()
        appendLine("IMPORTANT DISCLAIMER")
        appendLine("---------------------")
        appendLine(
            "No known threat found does not guarantee that a URL is safe."
        )
        appendLine(
            "This report reflects the results returned by the configured threat-intelligence sources at scan time."
        )

        appendLine()
        appendLine("Generated by ScamShield.")
    }
}

// LINK SCANNER SCREEN

@Composable
fun LinkScannerScreen(
    onBack: () -> Unit,
    sharedText: String? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var pendingPdfReport by remember {
        mutableStateOf("")
    }

    var pdfSaveMessage by remember {
        mutableStateOf("")
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->

        if (uri != null) {

            runCatching {
                saveScanReportPdf(
                    context,
                    uri,
                    pendingPdfReport
                )
            }.onSuccess {

                pdfSaveMessage = "PDF report saved successfully."

            }.onFailure { error ->

                pdfSaveMessage =
                    "PDF save failed: ${error.message ?: "Unknown error"}"
            }

        } else {
            pdfSaveMessage = "PDF saving cancelled."
        }
    }

    var url by remember(sharedText) {
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
            HISTORY_PREFS,
            Context.MODE_PRIVATE
        )
    }

    var scanHistory by remember {
        mutableStateOf(
            runCatching {
                val saved = prefs.getString(
                    HISTORY_KEY,
                    "[]"
                ) ?: "[]"

                val array = JSONArray(saved)

                List(array.length()) { index ->
                    array.getString(index)
                }
            }.getOrDefault(emptyList())
        )
    }

    var result by remember {
        mutableStateOf<ScanDisplayResult?>(null)
    }

    var resultMessage by remember {
        mutableStateOf("")
    }

    var isChecking by remember {
        mutableStateOf(false)
    }

    var lastScanTime by remember {
        mutableStateOf("Not available")
    }

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
                pdfSaveMessage = ""
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
                        pdfSaveMessage = ""

                        resultMessage =
                            "Checking URL with threat intelligence sources..."

                        try {

                            val scan = withContext(Dispatchers.IO) {
                                performUrlScan(candidate)
                            }

                            result = scan

                            resultMessage = when {

                                scan.verdict == "LIKELY_MALICIOUS" ->
                                    "LIKELY MALICIOUS"

                                scan.verdict == "NO_KNOWN_THREAT_FOUND" &&
                                        scan.reliability == "2 SOURCES CHECKED" ->
                                    "NO KNOWN THREAT FOUND"

                                scan.verdict == "NO_KNOWN_THREAT_FOUND" ->
                                    "INCOMPLETE CHECK — NO CONFIRMED MATCH"

                                scan.verdict == "CHECK_UNAVAILABLE" ->
                                    "CHECK UNAVAILABLE"

                                scan.verdict == "INVALID_URL" ->
                                    "INVALID URL"

                                scan.verdict == "DOMAIN_NOT_FOUND" ->
                                    "DOMAIN NOT FOUND"

                                scan.verdict == "DOMAIN_STATUS_UNCONFIRMED" ->
                                    "DOMAIN STATUS UNCONFIRMED"

                                else ->
                                    "SCAN RESULT: ${scan.verdict}"
                            }

                            val scannedAt = SimpleDateFormat(
                                "dd MMM yyyy, hh:mm a",
                                Locale.getDefault()
                            ).format(Date())

                            lastScanTime = scannedAt

                            val sourceHistory = buildString {

                                append("URL: $candidate\n")
                                append("Scanned: $scannedAt\n")
                                append("Verdict: ${scan.verdict}\n")
                                append("Reliability: ${scan.reliability}\n")
                                append("HTTP status: ${scan.httpCode}\n")
                                append("Message: ${scan.message}\n")

                                scan.sources.forEach { source ->

                                    append(
                                        "${source.name}: ${
                                            when {
                                                source.matched && source.checked ->
                                                    "THREAT DETECTED"

                                                source.checked ->
                                                    "No known match"

                                                else ->
                                                    "Not checked"
                                            }
                                        }\n"
                                    )
                                }

                                scan.warnings.forEach { warning ->
                                    append("Warning: $warning\n")
                                }
                            }

                            scanHistory = (
                                    listOf(sourceHistory) + scanHistory
                                    ).take(MAX_SCAN_HISTORY)

                        } catch (e: Exception) {

                            result = null

                            resultMessage =
                                "CHECK FAILED\n\n${e.message ?: "Unknown connection error"}"

                            val scannedAt = SimpleDateFormat(
                                "dd MMM yyyy, hh:mm a",
                                Locale.getDefault()
                            ).format(Date())

                            lastScanTime = scannedAt

                            scanHistory = (
                                    listOf(
                                        "URL: $candidate\nScanned: $scannedAt\n$resultMessage"
                                    ) + scanHistory
                                    ).take(MAX_SCAN_HISTORY)

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

            val scan = result

            val verdictColor = when {

                scan?.verdict == "LIKELY_MALICIOUS" ->
                    CyberRed

                scan?.verdict == "NO_KNOWN_THREAT_FOUND" &&
                        scan.reliability == "2 SOURCES CHECKED" ->
                    CyberGreen

                else ->
                    CyberAmber
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

                scan?.let {

                    HorizontalDivider(color = Color.DarkGray)

                    Text(
                        "SCAN RELIABILITY",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        it.reliability,
                        color = if (
                            it.reliability == "2 SOURCES CHECKED"
                        ) {
                            CyberGreen
                        } else {
                            CyberAmber
                        },
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider(color = Color.DarkGray)

                    Text(
                        "SOURCE STATUS",
                        color = CyberGreen,
                        fontWeight = FontWeight.Bold
                    )

                    if (it.sources.isEmpty()) {

                        Text(
                            "No threat-intelligence source returned a result.",
                            color = CyberAmber,
                            fontSize = 13.sp
                        )

                    } else {

                        it.sources.forEach { source ->

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
                                        source.checked && source.matched ->
                                            "THREAT FOUND"

                                        source.checked ->
                                            "NO MATCH"

                                        else ->
                                            "NOT CHECKED"
                                    },
                                    color = when {
                                        source.checked && source.matched ->
                                            CyberRed

                                        source.checked ->
                                            CyberGreen

                                        else ->
                                            CyberAmber
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color.DarkGray)

                    Text(
                        it.message,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )

                    if (it.verdict == "LIKELY_MALICIOUS") {
                        Text(
                            "Recommendation: Do not open this link.",
                            color = CyberRed,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (it.verdict == "NO_KNOWN_THREAT_FOUND") {
                        Text(
                            "A negative result does not guarantee that this URL is safe. New, unreported or undetected threats may not appear in these databases.",
                            color = CyberAmber,
                            fontSize = 13.sp
                        )
                    }

                    if (it.reliability != "2 SOURCES CHECKED") {
                        Text(
                            "This scan is incomplete. Do not treat the absence of a confirmed match as evidence that the URL is safe.",
                            color = CyberAmber,
                            fontSize = 13.sp
                        )
                    }

                    if (it.warnings.isNotEmpty()) {

                        Text(
                            "WARNINGS",
                            color = CyberAmber,
                            fontWeight = FontWeight.Bold
                        )

                        it.warnings.forEach { warning ->

                            Text(
                                "• $warning",
                                color = CyberAmber,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        "Scan time: $lastScanTime",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    Text(
                        "HTTP status: ${it.httpCode}",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    // SHARE SCAN REPORT

                    Button(
                        onClick = {
                            val report = createScanReport(
                                url,
                                lastScanTime,
                                it
                            )

                            shareScanReport(context, report)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Share Scan Report")
                    }

                    // SAVE SCAN REPORT AS PDF

                    Button(
                        onClick = {

                            pendingPdfReport = createScanReport(
                                url,
                                lastScanTime,
                                it
                            )

                            pdfSaveMessage = ""

                            val fileName = "ScamShield_Report.pdf"

                            pdfLauncher.launch(fileName)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Report as PDF")
                    }

                    if (pdfSaveMessage.isNotBlank()) {
                        Text(
                            pdfSaveMessage,
                            color = if (
                                pdfSaveMessage.contains("successfully")
                            ) {
                                CyberGreen
                            } else {
                                CyberAmber
                            },
                            fontSize = 13.sp
                        )
                    }
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

        Text(
            "Saved records: ${scanHistory.size}",
            color = Color.LightGray,
            fontSize = 12.sp
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
                        savedUrl.ifBlank { "Scan record" },
                        color = CyberGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        if (expanded) "Hide details ▲"
                        else "View details ▼",
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
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Copy URL")
                            }

                            OutlinedButton(
                                onClick = {
                                    scanHistory = scanHistory.filterIndexed {
                                            itemIndex, _ ->
                                        itemIndex != index
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "Delete",
                                    color = CyberRed
                                )
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    showClearHistoryDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Clear All Scan History",
                    color = CyberRed
                )
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
                Text(
                    "This will permanently remove all saved scan records from this app."
                )
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
            .putString(HISTORY_KEY, array.toString())
            .apply()
    }
}
