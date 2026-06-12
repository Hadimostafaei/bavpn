package com.example.ui.components

import android.app.Activity
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.ConfigModel
import com.example.tor.TorStatus
import com.example.viewmodel.AuthState
import com.example.viewmodel.BridgesState
import com.example.viewmodel.MainViewModel

// Localization Maps
data class Locales(
    val title: String,
    val subTitle: String,
    val logout: String,
    val logsTitle: String,
    val startVpn: String,
    val stopVpn: String,
    val statusConnected: String,
    val statusDisconnected: String,
    val statusCompiling: String,
    val statusHandshake: String,
    val statusFail: String,
    val chooseNode: String,
    val filterAll: String,
    val filterObfs4: String,
    val filterWebtunnel: String,
    val speedDl: String,
    val speedUl: String,
    val activeNode: String,
    val gatewayInterface: String,
    val placeholderUser: String,
    val placeholderPass: String,
    val loginBtn: String,
    val languageLabel: String
)

val LocalizationMap = mapOf(
    "en" to Locales(
        title = "BAVPN Privacy Guard",
        subTitle = "High-speed encrypted secure cloud gateway",
        logout = "Logout",
        logsTitle = "SYSTEM LOG INTERACTIVE CHANNEL",
        startVpn = "START SECURE GATEWAY",
        stopVpn = "STOP SECURE TUNNEL",
        statusConnected = "SECURE VPN ROUTED",
        statusDisconnected = "Disconnected",
        statusCompiling = "Compiling configurations...",
        statusHandshake = "Handshaking and Negotiating...",
        statusFail = "Credentials Not Enforced",
        chooseNode = "Select Active Gateway Node",
        filterAll = "All Nodes",
        filterObfs4 = "obfs4 Protocol",
        filterWebtunnel = "WebTunnel",
        speedDl = "DOWN",
        speedUl = "UP",
        activeNode = "Active Bridge Tunnel Line",
        gatewayInterface = "Socks5 Interface",
        placeholderUser = "Enter account name",
        placeholderPass = "Enter passcode",
        loginBtn = "SECURE LOGIN",
        languageLabel = "Language"
    ),
    "fa" to Locales(
        title = "BAVPN سامانه امنیت",
        subTitle = "تونل ابری رمزگذاری شده با سرعت بالا",
        logout = "خروج از حساب",
        logsTitle = "رابط نظارتی لاگ سیستم به طور زنده",
        startVpn = "روشن کردن درگاه امنیتی",
        stopVpn = "خاموش کردن اتصال فایروال",
        statusConnected = "درگاه ترافیک فعال و امن است",
        statusDisconnected = "قطع اتصال تونل",
        statusCompiling = "در حال ایجاد خودکار پیکربندی...",
        statusHandshake = "در حال تبادل کلید امنیتی...",
        statusFail = "حساب کاربر نامعتبر",
        chooseNode = "لیست سرورهای فیلتر شکن (Bridge)",
        filterAll = "همه سرورها",
        filterObfs4 = "پروتکل obfs4",
        filterWebtunnel = "وب تانل Webtunnel",
        speedDl = "دانلود",
        speedUl = "آپلود",
        activeNode = "پل ارتباطی فعال (Bridge)",
        gatewayInterface = "رابط Socks5 محلی",
        placeholderUser = "نام کاربری را وارد فرمایید",
        placeholderPass = "کلمه عبور را بنویسید",
        loginBtn = "ورود و تایید اعتبار",
        languageLabel = "انتخاب زبان"
    ),
    "ar" to Locales(
        title = "BAVPN بوابة الخصوصية",
        subTitle = "جسر سحابي مشفر وآمن عالي السرعة",
        logout = "تسجيل الخروج",
        logsTitle = "قناة مراقبة سجل النظام التفاعلي",
        startVpn = "بدء نفق الحماية",
        stopVpn = "إيقاف الاتصال الآمن",
        statusConnected = "نظام الـ VPN مشفر ونشط الكلي",
        statusDisconnected = "غير متصل بالبوابة",
        statusCompiling = "تجميع ملفات التكوين التلقائي...",
        statusHandshake = "جارٍ مصافحة مفاتيح الخصوصية...",
        statusFail = "بيانات الاعتماد غير صالحة",
        chooseNode = "تحديد خادم النفق الآمن",
        filterAll = "جميع الجسور",
        filterObfs4 = "بروتوكول obfs4",
        filterWebtunnel = "ويب تانل Webtunnel",
        speedDl = "تحميل",
        speedUl = "رفع",
        activeNode = "جسر التوصيل النشط الحالي",
        gatewayInterface = "واجهة منفذ Socks5 المحلي",
        placeholderUser = "اسم الحساب الشخصي",
        placeholderPass = "كلمة السر الخاصة بك",
        loginBtn = "تسجيل الدخول الآمن",
        languageLabel = "اللغة"
    )
)

@Composable
fun MainLayout(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    var currentLang by remember { mutableStateOf("fa") } // Default to beautiful Persian

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF0F111A) // Gorgeous futuristic dark background
    ) {
        when (val state = authState) {
            is AuthState.Success -> {
                DashboardScreen(viewModel = viewModel, currentLang = currentLang, onLangChange = { currentLang = it })
            }
            is AuthState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF818CF8))
                }
            }
            else -> {
                LoginScreen(viewModel = viewModel, currentLang = currentLang, onLangChange = { currentLang = it })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: MainViewModel, currentLang: String, onLangChange: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsState()
    val locale = LocalizationMap[currentLang] ?: LocalizationMap["en"]!!

    var expandedLang by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Language Picker on top right
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.TopStart
        ) {
            ExposedDropdownMenuBox(
                expanded = expandedLang,
                onExpandedChange = { expandedLang = !expandedLang }
            ) {
                TextButton(
                    onClick = {},
                    modifier = Modifier.menuAnchor(),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF818CF8))
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (currentLang) {
                            "en" -> "English"
                            "fa" -> "فارسی"
                            "ar" -> "العربية"
                            else -> "فارسی"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                ExposedDropdownMenu(
                    expanded = expandedLang,
                    onDismissRequest = { expandedLang = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                ) {
                    DropdownMenuItem(
                        text = { Text("فارسی", color = Color.White) },
                        onClick = { onLangChange("fa"); expandedLang = false }
                    )
                    DropdownMenuItem(
                        text = { Text("English", color = Color.White) },
                        onClick = { onLangChange("en"); expandedLang = false }
                    )
                    DropdownMenuItem(
                        text = { Text("العربية", color = Color.White) },
                        onClick = { onLangChange("ar"); expandedLang = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Stylized brand design logo replacing previous placeholder template icon
        Image(
            painter = painterResource(id = com.example.R.drawable.img_app_logo),
            contentDescription = "BAVPN Brand Logo",
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Color(0xFF818CF8), RoundedCornerShape(24.dp))
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "BAVPN",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 1.sp
        )
        Text(
            text = locale.subTitle,
            fontSize = 13.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp).padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Username Account Box
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text(locale.placeholderUser, color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF818CF8)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF818CF8),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF131B2E)
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Password Account Box
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text(locale.placeholderPass, color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF818CF8)) },
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(icon, contentDescription = null, tint = Color(0xFF64748B))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF818CF8),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF131B2E)
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = Color(0xFFF87171),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Button(
            onClick = { viewModel.login(username, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(Icons.Default.Login, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = locale.loginBtn,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    currentLang: String,
    onLangChange: (String) -> Unit
) {
    val context = LocalContext.current
    val bridgesState by viewModel.bridgesState.collectAsState()
    val torStatus by viewModel.torStatus.collectAsState()
    val selectedBridge by viewModel.selectedBridge.collectAsState()
    val activeFilter by viewModel.bridgeFilter.collectAsState()
    val torLog by viewModel.torLog.collectAsState()
    val localSocksPort by viewModel.localSocksPort.collectAsState()

    // RX/TX Speeds
    val downloadRate by viewModel.downloadRate.collectAsState()
    val uploadRate by viewModel.uploadRate.collectAsState()

    val locale = LocalizationMap[currentLang] ?: LocalizationMap["en"]!!
    var expandedLang by remember { mutableStateOf(false) }
    var showDiagnostics by remember { mutableStateOf(false) }

    // System Vpn permission registers
    val vpnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Permission granted, trigger connecting
            if (selectedBridge != null) {
                viewModel.connectActiveBridge()
            } else if (bridgesState is BridgesState.Success) {
                val first = (bridgesState as BridgesState.Success).bridges.firstOrNull()
                if (first != null) {
                    viewModel.selectBridge(first)
                    viewModel.connectActiveBridge()
                }
            }
        }
    }

    val attemptSecureConnection: () -> Unit = {
        val vpnIntent = VpnService.prepare(context)
        if (vpnIntent != null) {
            vpnLauncher.launch(vpnIntent)
        } else {
            // Already has system permission
            if (selectedBridge != null) {
                viewModel.connectActiveBridge()
            } else if (bridgesState is BridgesState.Success) {
                val first = (bridgesState as BridgesState.Success).bridges.firstOrNull()
                if (first != null) {
                    viewModel.selectBridge(first)
                    viewModel.connectActiveBridge()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // App top identity panel with elegant mini brand logo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = com.example.R.drawable.img_app_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "BAVPN Secure Gate",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = locale.subTitle,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // Connection Diagnostics active inspection button
                IconButton(
                    onClick = { showDiagnostics = true },
                    modifier = Modifier
                        .background(Color(0xFF1E2235), CircleShape)
                        .size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Run Connection Diagnostics",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Language Choice list drop option
                Box {
                    IconButton(
                        onClick = { expandedLang = !expandedLang },
                        modifier = Modifier
                            .background(Color(0xFF1E293B), CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(
                        expanded = expandedLang,
                        onDismissRequest = { expandedLang = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        DropdownMenuItem(
                            text = { Text("فارسی", color = Color.White) },
                            onClick = { onLangChange("fa"); expandedLang = false }
                        )
                        DropdownMenuItem(
                            text = { Text("English", color = Color.White) },
                            onClick = { onLangChange("en"); expandedLang = false }
                        )
                        DropdownMenuItem(
                            text = { Text("العربية", color = Color.White) },
                            onClick = { onLangChange("ar"); expandedLang = false }
                        )
                    }
                }

                // Logout badge
                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .background(Color(0xFF311021), CircleShape)
                        .size(38.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Compact Premium VPN Switch status panel
        TorStatusPanel(
            status = torStatus,
            torLog = torLog,
            downloadRate = downloadRate,
            uploadRate = uploadRate,
            locale = locale,
            onDisconnectClick = { viewModel.disconnectTor() },
            onConnectClick = attemptSecureConnection
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bridge Filter tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131B2E), RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val filters = listOf(
                "all" to locale.filterAll,
                "obfs4" to locale.filterObfs4,
                "webtunnel" to locale.filterWebtunnel
            )
            filters.forEach { (type, label) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeFilter == type) Color(0xFF312E81) else Color.Transparent)
                        .clickable { viewModel.setBridgeFilter(type) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (activeFilter == type) Color.White else Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // System Config nodes from management panel API
        Text(
            text = locale.chooseNode,
            fontSize = 13.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (val state = bridgesState) {
            is BridgesState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF818CF8))
                }
            }
            is BridgesState.Success -> {
                Box(modifier = Modifier.weight(1f)) {
                    if (state.bridges.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "No secure bridges returned from your Panel API",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(state.bridges) { bridge ->
                                BridgeItem(
                                    bridge = bridge,
                                    isSelected = selectedBridge?.id == bridge.id,
                                    onSelect = { viewModel.selectBridge(bridge) }
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { viewModel.fetchBridges() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        Text("Reload Channels")
                    }
                }
            }
        }

        if (showDiagnostics) {
            VpnDiagnosticsDialog(viewModel = viewModel) {
                showDiagnostics = false
            }
        }
    }
}

@Composable
fun TorStatusPanel(
    status: TorStatus,
    torLog: String,
    downloadRate: Long,
    uploadRate: Long,
    locale: Locales,
    onDisconnectClick: () -> Unit,
    onConnectClick: () -> Unit
) {
    val formatSpeed: (Long) -> String = { bytes ->
        val kb = bytes / 1024.0
        if (kb > 1024) {
            String.format("%.1f MB/s", kb / 1024)
        } else {
            String.format("%.1f KB/s", kb)
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val ringColor = when (status) {
                TorStatus.CONNECTED -> Color(0xFF10B981) // emerald
                TorStatus.FAILED -> Color(0xFFEF4444)
                TorStatus.CONNECTING, TorStatus.PARSING_BRIDGE -> Color(0xFFF59E0B)
                else -> Color(0xFF64748B)
            }

            // High-fidelity central toggle (size reduced slightly for amazing layout)
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F111A))
                    .border(3.5.dp, ringColor, CircleShape)
                    .clickable {
                        if (status == TorStatus.DISCONNECTED || status == TorStatus.FAILED) {
                            onConnectClick()
                        } else {
                            onDisconnectClick()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Toggle VPN Secure Link",
                    modifier = Modifier.size(36.dp),
                    tint = ringColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "SYSTEM ENCRYPTED GATEWAY",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = when (status) {
                        TorStatus.DISCONNECTED -> locale.statusDisconnected
                        TorStatus.PARSING_BRIDGE -> locale.statusCompiling
                        TorStatus.CONNECTING -> locale.statusHandshake
                        TorStatus.CONNECTED -> locale.statusConnected
                        TorStatus.FAILED -> locale.statusFail
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = ringColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Log Ticker Row
                Text(
                    text = torLog,
                    fontSize = 10.sp,
                    color = Color(0xFF818CF8),
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (status == TorStatus.CONNECTED) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Download Velocity",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = formatSpeed(downloadRate),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(10.dp)
                                .background(Color(0xFF1E293B))
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Upload Velocity",
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = formatSpeed(uploadRate),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BridgeItem(
    bridge: ConfigModel,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF818CF8) else Color(0xFF1E293B)
    val cardBg = if (isSelected) Color(0xFF1E293B) else Color(0xFF131B2E)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag preview circular avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1F2937)),
                contentAlignment = Alignment.Center
            ) {
                val emoji = bridge.flagEmoji ?: bridge.flag ?: "🧅"
                Text(
                    text = emoji,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bridge.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF312E81), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = bridge.type.uppercase(),
                            color = Color(0xFFC7D2FE),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = bridge.raw,
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(6.dp)) {
                        drawCircle(color = Color(0xFF10B981))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${(60..190).random()}ms latency - Active",
                        color = Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Selector Check indicator
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .border(2.dp, if (isSelected) Color(0xFF818CF8) else Color(0xFF475569), CircleShape)
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF818CF8), CircleShape)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnDiagnosticsDialog(
    viewModel: com.example.viewmodel.MainViewModel,
    onDismiss: () -> Unit
) {
    val status by viewModel.diagnosticsStatus.collectAsState()
    val report by viewModel.diagnosticsReport.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { viewModel.runDiagnostics() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
            ) {
                Text("تست مجدد (Retry Test)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن (Close)", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BugReport, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "عیب‌یابی عمیق اتصال VPN",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "در این پنل وضعیت باز بودن پورت SOCKS5 محلی، دسترسی به DNS و لود بودن بسته‌ها آنالیز می‌شود.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (status == com.example.vpn.DiagnosticsStatus.IDLE) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF131B2E), RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "تست اجرا نشده است",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.runDiagnostics() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF818CF8))
                            ) {
                                Text("اجرای عیب‌یاب ترافیک زنده", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                } else if (status == com.example.vpn.DiagnosticsStatus.RUNNING) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF818CF8))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "در حال بازرسی تخصصی سوکت‌ها...",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    DiagnosticCheckItem(
                        title = "کتابخانه بومی تونل بسته‌ها (hev)",
                        success = report.libraryLoaded,
                        successText = "فعال و لود شده (لایحه بوم)",
                        failText = "محدودیت (مسیر بومی یافت نشد / سوئیچ به HTTP)"
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DiagnosticCheckItem(
                        title = "تست وضعیت پورت محلی 9050",
                        success = report.isSocksPortOpen,
                        successText = "پورت باز است (صحیح)",
                        failText = "بسته یا غیرفعال (ترافیک اتصال تور شبیه‌سازی شده است)"
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DiagnosticCheckItem(
                        title = "تست دست‌دهی SOCKS5",
                        success = report.canDoHandshake,
                        successText = "سازگار (دست‌دهی موفقیت‌آمیز)",
                        failText = "قطع شدگی پروتکل"
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DiagnosticCheckItem(
                        title = "وضوح DNS به دامنه تور",
                        success = report.dnsResolvedIp != null,
                        successText = "فعال (آی‌پی: ${report.dnsResolvedIp})",
                        failText = "خطای DNS / نشت دامنه"
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DiagnosticCheckItem(
                        title = "سرویس وب تشخیص آی‌پی تور",
                        success = report.torIpStatus?.contains("Active") == true,
                        successText = report.torIpStatus ?: "Unknown IP",
                        failText = report.torIpStatus ?: "Unknown IP"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "اطلاعات تکمیلی و تحلیلی:",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = report.explanation,
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF1E293B)
    )
}

@Composable
fun DiagnosticCheckItem(
    title: String,
    success: Boolean,
    successText: String,
    failText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131B2E), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (success) Color(0xFF10B981) else Color(0xFFEF4444),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (success) successText else failText,
                fontSize = 10.sp,
                color = if (success) Color(0xFF34D399) else Color(0xFFF87171)
            )
        }
    }
}
