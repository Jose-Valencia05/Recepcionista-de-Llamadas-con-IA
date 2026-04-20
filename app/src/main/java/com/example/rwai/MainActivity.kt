package com.example.rwai

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.telecom.TelecomManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Representa un registro de llamada individual.
 */
data class CallRecord(
    val number: String,
    val name: String,
    val callType: Int,
    val timestamp: String,
    var frequency: Int = 1
)

/**
 * Clase encargada de la extracción de datos del historial de llamadas del sistema.
 */
object CallLogProvider {
    /**
     * Recupera y procesa el historial de llamadas reciente.
     */
    fun refreshCallLogs(context: Context) {
        CallRecordStore.historyRecords.clear()
        CallRecordStore.notificationRecords.clear()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) return

        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME, CallLog.Calls.TYPE, CallLog.Calls.DATE),
            null, null, CallLog.Calls.DATE + " DESC"
        )

        val frequencyMap = mutableMapOf<String, CallRecord>()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        cursor?.use {
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)

            while (it.moveToNext()) {
                val number = it.getString(numberIndex) ?: "Unknown"
                val name = it.getString(nameIndex) ?: "Unknown"
                val type = it.getInt(typeIndex)
                val epochTime = it.getLong(dateIndex)
                val timeString = timeFormat.format(Date(epochTime))

                if (frequencyMap.containsKey(number)) {
                    frequencyMap[number]!!.frequency++
                } else {
                    frequencyMap[number] = CallRecord(number, name, type, timeString, 1)
                }
            }
        }

        frequencyMap.values.forEach { record ->
            if (record.callType == CallLog.Calls.MISSED_TYPE ||
                record.callType == CallLog.Calls.REJECTED_TYPE ||
                record.callType == CallLog.Calls.BLOCKED_TYPE) {
                CallRecordStore.notificationRecords.add(record)
            } else {
                CallRecordStore.historyRecords.add(record)
            }
        }
    }
}

/**
 * Almacén en memoria de los registros de llamadas procesados.
 */
object CallRecordStore {
    val historyRecords = mutableStateListOf<CallRecord>()
    val notificationRecords = mutableStateListOf<CallRecord>()

    fun getFavorites(): List<CallRecord> {
        return historyRecords.sortedByDescending { it.frequency }.take(3)
    }

    fun findMatch(number: String): CallRecord? {
        val cleanNumber = number.replace(" ", "")
        if (cleanNumber.isEmpty()) return null
        return historyRecords.find { it.number.replace(" ", "").contains(cleanNumber) }
            ?: notificationRecords.find { it.number.replace(" ", "").contains(cleanNumber) }
    }
}

class MainActivity : ComponentActivity() {
    private val isRoleHeld = mutableStateOf(false)

    private val requestRoleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            isRoleHeld.value = true
            CallLogProvider.refreshCallLogs(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
        isRoleHeld.value = roleManager.isRoleHeld(RoleManager.ROLE_DIALER)

        if (isRoleHeld.value) {
            CallLogProvider.refreshCallLogs(this)
        }

        setContent {
            MainScreen(
                hasControl = isRoleHeld.value,
                onPermissionRequestClick = {
                    if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                        requestRoleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER))
                    }
                }
            )
        }
    }
}

@Composable
fun MainScreen(hasControl: Boolean, onPermissionRequestClick: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var searchQuery by remember { mutableStateOf("") }
    var isKeyboardVisible by remember { mutableStateOf(false) }

    val topBarPadding by animateDpAsState(targetValue = if (isKeyboardVisible) 100.dp else 16.dp, animationSpec = tween(400), label = "")

    val currentMatch = CallRecordStore.findMatch(searchQuery)
    val favorites = CallRecordStore.getFavorites()

    BackHandler(enabled = isKeyboardVisible) {
        isKeyboardVisible = false
        searchQuery = ""
        focusManager.clearFocus()
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(visible = !isKeyboardVisible) {
                BottomAppBar(containerColor = Color(0xFF1B1B1B), modifier = Modifier.height(80.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            favorites.forEach { record ->
                                FavoriteButton(record) {
                                    searchQuery = record.number
                                    isKeyboardVisible = true
                                }
                            }
                        }
                        FloatingActionButton(
                            onClick = { isKeyboardVisible = true },
                            containerColor = Color(0xFF78DC77),
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp)
                        ) { Icon(Icons.Default.Dialpad, contentDescription = null, tint = Color(0xFF00390A)) }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF131313)).padding(paddingValues)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    focusManager.clearFocus()
                }
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = !isKeyboardVisible) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(24.dp))
                        if (!hasControl) {
                            Button(onClick = onPermissionRequestClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF78DC77))) {
                                Text("Set Default Dialer", color = Color(0xFF00390A))
                            }
                        } else {
                            Text("🛡️ Service Active", color = Color(0xFF78DC77), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(topBarPadding))

                AnimatedVisibility(visible = isKeyboardVisible && currentMatch != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val initials = currentMatch?.name?.take(2)?.uppercase() ?: "??"
                        val isNegative = (currentMatch?.callType == CallLog.Calls.REJECTED_TYPE || currentMatch?.callType == CallLog.Calls.MISSED_TYPE)

                        Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(if (isNegative) Color(0xFFFFB3AE) else Color(0xFF2A2A2A)), contentAlignment = Alignment.Center) {
                            Text(initials, color = if (isNegative) Color(0xFF131313) else Color(0xFF78DC77), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(currentMatch?.name ?: "", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(28.dp)).background(Color(0xFF1B1B1B)).border(1.dp, Color(0xFF3F4A3C).copy(alpha = 0.5f), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp),
                            cursorBrush = SolidColor(Color(0xFF78DC77)),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().onFocusChanged { if (it.isFocused) isKeyboardVisible = true },
                            decorationBox = { innerTextField -> if (searchQuery.isEmpty()) Text("Search contact or number...", color = Color.Gray) else innerTextField() }
                        )
                    }
                }

                AnimatedVisibility(visible = isKeyboardVisible && currentMatch != null) {
                    val isNegative = (currentMatch?.callType == CallLog.Calls.REJECTED_TYPE || currentMatch?.callType == CallLog.Calls.MISSED_TYPE)
                    val freqText = if (isNegative) "MISSED/REJECTED ${currentMatch?.frequency} TIMES" else "ANSWERED ${currentMatch?.frequency} TIMES"
                    val freqColor = if (isNegative) Color(0xFFFFB3AE) else Color(0xFF78DC77)
                    Text(freqText, color = freqColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(top = 16.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isKeyboardVisible) {
                    Spacer(modifier = Modifier.weight(1f))
                    Dialpad(
                        onDigitPressed = { searchQuery += it },
                        onDelete = {
                            if (searchQuery.isNotEmpty()) searchQuery = searchQuery.dropLast(1)
                        },
                        onCall = {
                            if (searchQuery.isNotEmpty()) {
                                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                                try {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                        telecomManager.placeCall(Uri.fromParts("tel", searchQuery, null), null)
                                    }
                                } catch (e: Exception) { Log.e("App", "Call failed") }
                            }
                        }
                    )
                } else {
                    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                        Text("Recent Activity", color = Color(0xFFFFB3AE), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp).wrapContentHeight()) {
                            items(CallRecordStore.notificationRecords) { record -> RecordItem(record) }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Call History", color = Color(0xFF78DC77), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp).wrapContentHeight()) {
                            items(CallRecordStore.historyRecords) { record -> RecordItem(record) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Dialpad(onDigitPressed: (String) -> Unit, onDelete: () -> Unit, onCall: () -> Unit) {
    val keys = listOf(
        Triple("1", "", ""), Triple("2", "A B C", ""), Triple("3", "D E F", ""),
        Triple("4", "G H I", ""), Triple("5", "J K L", ""), Triple("6", "M N O", ""),
        Triple("7", "P Q R S", ""), Triple("8", "T U V", ""), Triple("9", "W X Y Z", ""),
        Triple("*", "", ""), Triple("0", "+", ""), Triple("#", "", "")
    )

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).background(Color(0xFF0A0A0A)).padding(top = 24.dp, bottom = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
            for (row in 0..3) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (col in 0..2) {
                        val key = keys[row * 3 + col]
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF1B1B1B)).clickable { onDigitPressed(key.first) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(key.first, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Medium)
                                if (key.second.isNotEmpty()) Text(key.second, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.size(48.dp).padding(end = 32.dp))

                FloatingActionButton(
                    onClick = onCall, containerColor = Color(0xFF1B1B1B), contentColor = Color(0xFF78DC77),
                    shape = CircleShape, modifier = Modifier.size(72.dp)
                ) { Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(32.dp)) }

                Spacer(modifier = Modifier.width(32.dp))

                IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
fun RecordItem(record: CallRecord) {
    val (icon, iconColor) = when (record.callType) {
        CallLog.Calls.MISSED_TYPE -> Icons.Default.PhoneMissed to Color.White
        CallLog.Calls.REJECTED_TYPE, CallLog.Calls.BLOCKED_TYPE -> Icons.Default.Block to Color(0xFFFFB3AE)
        CallLog.Calls.OUTGOING_TYPE -> Icons.Default.CallMade to Color(0xFF78DC77)
        CallLog.Calls.INCOMING_TYPE -> Icons.Default.CallReceived to Color(0xFF78DC77)
        else -> Icons.Default.Call to Color.Gray
    }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF2A2A2A)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                if (record.name == "Unknown") {
                    Text(record.number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(record.timestamp, color = Color.Gray, fontSize = 12.sp)
                } else {
                    Text(record.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${record.number} • ${record.timestamp}", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FavoriteButton(record: CallRecord, onClick: () -> Unit) {
    val initials = record.name.take(2).uppercase()
    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF2A2A2A)).border(1.dp, Color(0xFF3F4A3C), CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(if (record.name == "Unknown") "#" else initials, color = Color.White, fontWeight = FontWeight.Bold)
    }
}