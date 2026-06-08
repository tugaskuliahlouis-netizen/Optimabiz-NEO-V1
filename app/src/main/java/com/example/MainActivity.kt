package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import coil.compose.AsyncImage
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Product
import com.example.data.User
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeuBlack
import com.example.ui.theme.NeuYellow
import com.example.ui.theme.NeuPink
import com.example.ui.theme.NeuBlue
import com.example.ui.theme.NeuGreen
import com.example.ui.theme.NeuOrange
import com.example.ui.theme.NeuPurple
import com.example.ui.theme.NeuCardBg
import com.example.ui.theme.NeuTextMuted
import com.example.ui.theme.LightGreyBg
import kotlinx.coroutines.delay
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContainer()
            }
        }
    }
}

// ============================================================
// NEUBRUTALIST REUSABLE WIDGETS & MODIFIERS
// ============================================================

fun Modifier.bottomBorder(width: Dp, color: Color): Modifier = this.drawBehind {
    val strokeWidth = width.toPx()
    val y = size.height - strokeWidth / 2
    drawLine(
        color = color,
        start = androidx.compose.ui.geometry.Offset(0f, y),
        end = androidx.compose.ui.geometry.Offset(size.width, y),
        strokeWidth = strokeWidth
    )
}

fun Modifier.topBorder(width: Dp, color: Color): Modifier = this.drawBehind {
    val strokeWidth = width.toPx()
    val y = strokeWidth / 2
    drawLine(
        color = color,
        start = androidx.compose.ui.geometry.Offset(0f, y),
        end = androidx.compose.ui.geometry.Offset(size.width, y),
        strokeWidth = strokeWidth
    )
}

fun Modifier.endBorder(width: Dp, color: Color): Modifier = this.drawBehind {
    val strokeWidth = width.toPx()
    val x = size.width - strokeWidth / 2
    drawLine(
        color = color,
        start = androidx.compose.ui.geometry.Offset(x, 0f),
        end = androidx.compose.ui.geometry.Offset(x, size.height),
        strokeWidth = strokeWidth
    )
}

@Composable
fun NeuCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = NeuBlack,
    shadowColor: Color = NeuBlack,
    shadowOffset: Dp = 3.dp,
    borderWidth: Dp = 2.dp,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
    ) {
        // Shadow (offset behind)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor, shape)
                .border(borderWidth, borderColor, shape)
        )
        // Main Container Card - always match parent size inside the parent Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = shadowOffset, bottom = shadowOffset)
                .background(backgroundColor, shape)
                .border(borderWidth, borderColor, shape)
                .then(clickModifier)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun NeuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeuYellow,
    shadowOffset: Dp = 3.dp,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    testTag: String? = null,
    fillMaxWidth: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        // Shadow Layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(NeuBlack, shape)
                .border(2.dp, NeuBlack, shape)
        )
        // Button Content Layer
        Row(
            modifier = Modifier
                .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
                .padding(end = shadowOffset, bottom = shadowOffset)
                .background(backgroundColor, shape)
                .border(2.dp, NeuBlack, shape)
                .padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
fun NeuOutlineButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    shadowOffset: Dp = 3.dp,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    content: @Composable RowScope.() -> Unit
) {
    NeuButton(
        onClick = onClick,
        modifier = modifier,
        backgroundColor = backgroundColor,
        shadowOffset = shadowOffset,
        shape = shape,
        content = content
    )
}

@Composable
fun NeuTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            color = NeuBlack,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        // Text Input field with Neubrutalist style and 12dp rounded corners
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = NeuBlack, fontWeight = FontWeight.Bold),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            cursorBrush = SolidColor(NeuBlack),
            decorationBox = @Composable { innerTextField ->
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(3.dp, NeuBlack, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .fillMaxWidth()
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        )
    }
}

// ============================================================
// MAIN APP COMPOSABLE ARCHITECTURE
// ============================================================

@Composable
fun MainContainer() {
    val context = LocalContext.current
    val viewModel: BusinessViewModel = viewModel {
        BusinessViewModel(context.applicationContext as Application)
    }

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val toastType by viewModel.toastType.collectAsStateWithLifecycle()

    // Autoslide Toast cleanup helper
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(3000)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF5)),
        containerColor = Color(0xFFFFFDF5)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Routing
            when (currentScreen) {
                AppScreen.LANDING -> LandingScreen(viewModel)
                AppScreen.LOGIN -> LoginScreen(viewModel)
                AppScreen.SIGNUP -> SignupScreen(viewModel)
                AppScreen.MAIN_APP -> MainAppLayout(viewModel)
            }

            // Toast Alert Overlay
            toastMessage?.let { msg ->
                val bannerColor = if (toastType == "success") NeuGreen else NeuPink
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 90.dp)
                        .padding(horizontal = 16.dp)
                        .background(bannerColor, RoundedCornerShape(12.dp))
                        .border(3.dp, NeuBlack, RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = msg,
                        color = NeuBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ============================================================
// SCREEN COMPOSABLES
// ============================================================

// ───────────────────────────────────────────
// 1. LANDING SCREEN
// ───────────────────────────────────────────
@Composable
fun LandingScreen(viewModel: BusinessViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge
            Box(
                modifier = Modifier
                    .background(NeuYellow, RoundedCornerShape(100.dp))
                    .border(2.dp, NeuBlack, RoundedCornerShape(100.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "⚡ Real-Action Tool untuk UMKM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeuBlack
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Large displays
            Text(
                text = "Aksi Nyata,\nBukan Teori",
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                color = NeuBlack,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ubah data produkmu menjadi strategi profit yang bisa langsung dieksekusi. Kelola inventaris, analisis margin, dan dapatkan rekomendasi AI yang konkret.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = NeuTextMuted,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Action CTA buttons
            NeuButton(
                onClick = { viewModel.setScreen(AppScreen.SIGNUP) },
                backgroundColor = NeuYellow,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text("🚀 Gaspol Sekarang", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeuBlack)
            }

            Spacer(modifier = Modifier.height(12.dp))

            NeuOutlineButton(
                onClick = { viewModel.setScreen(AppScreen.LOGIN) },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text("Sudah punya akun?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeuBlack)
            }
        }

        // Stats Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .topBorder(3.dp, NeuBlack)
                .bottomBorder(3.dp, NeuBlack)
                .background(Color.White)
        ) {
            val statBlocks = listOf(
                "200+" to "UMKM Aktif",
                "50K+" to "Produk",
                "99.9%" to "Uptime",
                "Free" to "Selamanya"
            )
            statBlocks.forEachIndexed { i, stat ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .then(if (i < statBlocks.lastIndex) Modifier.endBorder(2.dp, NeuBlack) else Modifier)
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stat.first,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = NeuYellow
                    )
                    Text(
                        text = stat.second,
                        fontSize = 10.sp,
                        color = NeuTextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Features list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "FITUR UNGGULAN",
                fontSize = 12.sp,
                color = NeuYellow,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Semua yang kamu butuhkan untuk scale up profit",
                style = MaterialTheme.typography.displayMedium,
                color = NeuBlack
            )
            Spacer(modifier = Modifier.height(24.dp))

            val features = listOf(
                Triple(Icons.Default.Inventory2, "Kelola Produk", "Tambah, edit, hapus produk lengkap dengan harga modal, harga jual, stok, dan kategori."),
                Triple(Icons.Default.Analytics, "Analisis Margin", "Hitung margin otomatis tiap produk. Identifikasi mana yang paling menguntungkan."),
                Triple(Icons.Default.Psychology, "Strategi AI", "Dapatkan rekomendasi pricing, inventory, dan fokus produk berbasis data nyata kamu."),
                Triple(Icons.Default.TrendingUp, "Dashboard Cuan", "Overview real-time nilai inventaris, rata-rata margin, dan alert stok rendah.")
            )

            features.forEach { feature ->
                NeuCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = Color.White
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(NeuYellow, RoundedCornerShape(8.dp))
                                .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = feature.first,
                                contentDescription = feature.second,
                                tint = NeuBlack,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(feature.second, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NeuBlack)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(feature.third, fontSize = 13.sp, color = NeuTextMuted)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // How it works
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "CARA KERJA",
                fontSize = 12.sp,
                color = NeuYellow,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "3 langkah ke profit lebih besar",
                style = MaterialTheme.typography.displayMedium,
                color = NeuBlack
            )
            Spacer(modifier = Modifier.height(24.dp))

            val steps = listOf(
                "Input Data Produkmu" to "Masukkan nama produk, harga modal, harga jual, dan stok. Beres dalam hitungan detik.",
                "Lihat Analisis Margin" to "OptimaBiz otomatis hitung margin semua produk dan tunjukkan mana yang perlu dioptimasi.",
                "Eksekusi Strategi AI" to "AI kami beri rekomendasi konkret: naikkan harga ini, fokus ke produk itu, bundle yang ini."
            )

            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, Color.Black.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(NeuYellow, RoundedCornerShape(8.dp))
                            .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                            .size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NeuBlack
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(step.first, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NeuBlack)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(step.second, fontSize = 13.sp, color = NeuTextMuted)
                    }
                }
            }
        }

        // CTA Section bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(NeuYellow, RoundedCornerShape(16.dp))
                .border(3.dp, NeuBlack, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Siap gaspol profit bisnismu?",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = NeuBlack,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Daftar gratis sekarang. Tidak perlu kartu kredit.",
                fontSize = 13.sp,
                color = NeuBlack.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            NeuButton(
                onClick = { viewModel.setScreen(AppScreen.SIGNUP) },
                backgroundColor = Color.White
            ) {
                Text(
                    text = "Mulai Sekarang — Gratis 🚀",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeuBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ───────────────────────────────────────────
// 2. LOGIN SCREEN
// ───────────────────────────────────────────
@Composable
fun LoginScreen(viewModel: BusinessViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Back link
        Row(
            modifier = Modifier
                .clickable { viewModel.setScreen(AppScreen.LANDING) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = NeuBlack)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Kembali ke beranda", color = NeuBlack, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Masuk ke Akun",
            style = MaterialTheme.typography.displayMedium,
            color = NeuBlack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Belum punya akun?",
            fontSize = 14.sp,
            color = NeuTextMuted
        )
        Text(
            text = "Daftar gratis di sini →",
            fontSize = 14.sp,
            color = NeuYellow,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { viewModel.setScreen(AppScreen.SIGNUP) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Demo login card hint
        NeuCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeuYellow
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Demo Mode",
                        tint = NeuBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COBA DEMO SEKALI KLIK:",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = NeuBlack
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.clickable {
                        email = "demo@optimabiz.com"
                        password = "demo123"
                    }
                ) {
                    Text(
                        text = "Klik di sini untuk mengisi data demo akun",
                        fontSize = 12.sp,
                        color = NeuBlack,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        NeuTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "email@umkm.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            testTag = "email_input"
        )

        Spacer(modifier = Modifier.height(16.dp))

        NeuTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "••••••••",
            isPassword = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            testTag = "password_input"
        )

        Spacer(modifier = Modifier.height(30.dp))

        NeuButton(
            onClick = { viewModel.handleLogin(email, password) },
            backgroundColor = NeuYellow,
            testTag = "login_button"
        ) {
            Text(
                text = "Masuk Sekarang →",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = NeuBlack
            )
        }
    }
}

// ───────────────────────────────────────────
// 3. SIGNUP SCREEN
// ───────────────────────────────────────────
@Composable
fun SignupScreen(viewModel: BusinessViewModel) {
    var bizName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("F&B") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var expandedCat by remember { mutableStateOf(false) }

    val categories = listOf("F&B", "Fashion", "Elektronik", "Kecantikan", "Kerajinan", "Lainnya")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Back link
        Row(
            modifier = Modifier
                .clickable { viewModel.setScreen(AppScreen.LANDING) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = NeuBlack)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Kembali ke beranda", color = NeuBlack, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Buat Akun Gratis",
            style = MaterialTheme.typography.displayMedium,
            color = NeuBlack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sudah punya akun?",
            fontSize = 14.sp,
            color = NeuTextMuted
        )
        Text(
            text = "Masuk di sini →",
            fontSize = 14.sp,
            color = NeuYellow,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { viewModel.setScreen(AppScreen.LOGIN) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        NeuTextField(
            value = bizName,
            onValueChange = { bizName = it },
            label = "Nama UMKM / Toko",
            placeholder = "Contoh: Toko Kopi Kami"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown Selector with Neubrutalist style
        Column {
            Text(
                text = "Kategori Bisnis",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = NeuTextMuted,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(3.dp, NeuBlack, RoundedCornerShape(12.dp))
                    .clickable { expandedCat = !expandedCat }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = getCategoryIconVector(category),
                            contentDescription = category,
                            tint = NeuBlack,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = category,
                            color = NeuBlack,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = if (expandedCat) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Pilih",
                        tint = NeuBlack
                    )
                }

                DropdownMenu(
                    expanded = expandedCat,
                    onDismissRequest = { expandedCat = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = getCategoryIconVector(cat),
                                        contentDescription = cat,
                                        tint = NeuBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(cat, color = NeuBlack, fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                category = cat
                                expandedCat = false
                            },
                            modifier = Modifier.background(Color.White)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        NeuTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "email@umkm.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        NeuTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "Min. 6 karakter",
            isPassword = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(30.dp))

        NeuButton(
            onClick = { viewModel.handleSignup(bizName, category, email, password) },
            backgroundColor = NeuYellow
        ) {
            Text(
                text = "Daftar Sekarang — Gratis 🚀",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = NeuBlack
            )
        }
    }
}

// ───────────────────────────────────────────
// 4. MAIN APP SCRRENS CONTAINER LAYOUT
// ───────────────────────────────────────────
@Composable
fun MainAppLayout(viewModel: BusinessViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val showProductDialog by viewModel.showProductDialog.collectAsStateWithLifecycle()
    val showDeleteConfirm by viewModel.showDeleteConfirm.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .bottomBorder(3.dp, NeuBlack)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OptimaBiz",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = NeuBlack,
                    modifier = Modifier.clickable { viewModel.setTab(AppTab.DASHBOARD) }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // User Badge
                    Box(
                        modifier = Modifier
                            .background(NeuYellow.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                            .border(2.dp, NeuBlack, RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🏪 ${currentUser?.bizName ?: "User"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeuBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 120.dp)
                        )
                    }
                    // Simple small logout
                    IconButton(
                        onClick = { viewModel.handleLogout() },
                        modifier = Modifier
                            .size(36.dp)
                            .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                            .background(Color.White, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Keluar",
                            tint = NeuPink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Scrollable App Page Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFFAF9F6))
            ) {
                when (currentTab) {
                    AppTab.DASHBOARD -> DashboardPage(viewModel)
                    AppTab.PRODUCTS -> ProductsPage(viewModel)
                    AppTab.ANALYSIS -> AnalysisPage(viewModel)
                    AppTab.STRATEGY -> StrategyPage(viewModel)
                    AppTab.PROFILE -> ProfilePage(viewModel)
                }
            }

            // Floating Neubrutalist Bottom Nav Bar aligned with Design Mockup
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAF9F6))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                NeuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White,
                    borderColor = NeuBlack,
                    shadowColor = NeuBlack,
                    shadowOffset = 4.dp,
                    borderWidth = 3.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val navTabs = listOf(
                            Triple(AppTab.DASHBOARD, Icons.Default.Home, "Home"),
                            Triple(AppTab.PRODUCTS, Icons.Default.Inventory2, "Produk"),
                            Triple(AppTab.ANALYSIS, Icons.Default.Analytics, "Analisis"),
                            Triple(AppTab.STRATEGY, Icons.Default.Psychology, "AI"),
                            Triple(AppTab.PROFILE, Icons.Default.Person, "Profil")
                        )

                        navTabs.forEach { tab ->
                            val isActive = currentTab == tab.first
                            val tabBg = if (isActive) NeuYellow else Color.White
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(tabBg)
                                        .then(
                                            if (isActive) Modifier.border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                                            else Modifier
                                        )
                                        .clickable { viewModel.setTab(tab.first) }
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = tab.second,
                                        contentDescription = tab.third,
                                        tint = NeuBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = tab.third,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = NeuBlack
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // DIALOGS & OVERLAYS
        // ==========================================

        // 1. Add/Edit Product Modal Dialog Overlay
        if (showProductDialog) {
            ProductEditDialog(viewModel)
        }

        // 2. Delete Confirmation Dialog Overlay
        if (showDeleteConfirm) {
            DeleteConfirmDialog(viewModel)
        }
    }
}

// ───────────────────────────────────────────
// APP TAB: 1. DASHBOARD PAGE
// ───────────────────────────────────────────
@Composable
fun DashboardPage(viewModel: BusinessViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val products by viewModel.productsList.collectAsStateWithLifecycle()

    val totalProducts = products.size
    val totalStock = products.sumOf { it.stock }
    val inventoryValue = products.sumOf { it.stock * it.costPrice }

    val avgMargin = if (products.isNotEmpty()) {
        products.map { p ->
            if (p.sellingPrice > 0) ((p.sellingPrice - p.costPrice) / p.sellingPrice) * 100 else 0.0
        }.average()
    } else {
        0.0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Dashboard Cuan",
                    tint = NeuBlack,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Dashboard Cuan",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeuBlack,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "Halo, ${currentUser?.bizName ?: "Maju Bersama"}! Yuk cek profit bisnismu sekarang.",
                fontSize = 13.sp,
                color = NeuTextMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Metrics Grid (implemented in stylized neubrutalist columns for safety)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Top row stats
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        StatMetricCard(Icons.Default.Inventory2, totalProducts.toString(), "Total Produk", NeuYellow)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StatMetricCard(Icons.Default.Layers, totalStock.toString(), "Total Stok", NeuBlue)
                    }
                }
                // Bottom row stats
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        StatMetricCard(Icons.Default.Payments, formatRupiahShort(inventoryValue), "Nilai Inventaris", NeuGreen)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StatMetricCard(Icons.Default.TrendingUp, "${String.format("%.1f", avgMargin)}%", "Avg Margin", NeuOrange)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // TOP PRODUCTS CARD
            NeuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏆 Top Produk (by Margin)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = NeuBlack
                        )
                        Text(
                            text = "Lihat Semua",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeuBlue,
                            modifier = Modifier
                                .clickable { viewModel.setTab(AppTab.PRODUCTS) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val topProducts = products.sortedByDescending { p ->
                        if (p.sellingPrice > 0) ((p.sellingPrice - p.costPrice) / p.sellingPrice) * 100 else 0.0
                    }.take(5)

                    if (topProducts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada data produk", fontSize = 13.sp, color = NeuTextMuted)
                        }
                    } else {
                        topProducts.forEachIndexed { index, product ->
                            val marginPercent = if (product.sellingPrice > 0) {
                                ((product.sellingPrice - product.costPrice) / product.sellingPrice) * 100
                            } else 0.0

                            val badgeColor = if (marginPercent >= 60) NeuGreen else if (marginPercent >= 30) NeuOrange else NeuPink

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(2.dp, color = NeuBlack, RoundedCornerShape(8.dp))
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Rank Number Badge
                                    Box(
                                        modifier = Modifier
                                            .background(NeuYellow, RoundedCornerShape(6.dp))
                                            .border(1.5.dp, NeuBlack, RoundedCornerShape(6.dp))
                                            .size(26.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (index + 1).toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = NeuBlack
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = product.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = NeuBlack,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = getCategoryIconVector(product.category),
                                                contentDescription = product.category,
                                                tint = NeuTextMuted,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "${product.category} · Stok: ${product.stock}",
                                                fontSize = 11.sp,
                                                color = NeuTextMuted
                                            )
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .background(badgeColor, RoundedCornerShape(6.dp))
                                        .border(1.5.dp, NeuBlack, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", marginPercent)}%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = NeuBlack
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // LOW STOCK ALERTS
            NeuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White
            ) {
                Column {
                    Text(
                        text = "⚠️ Alert Stok Rendah",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = NeuBlack
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val lowStockList = products.filter { it.stock <= 10 }
                    if (lowStockList.isEmpty()) {
                        Text(
                            text = "Semua stok aman ✅",
                            fontSize = 13.sp,
                            color = NeuGreen,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        lowStockList.forEach { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(NeuPink.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = NeuBlack
                                    )
                                    Text(
                                        text = "Stok tersisa: ${product.stock} unit",
                                        fontSize = 12.sp,
                                        color = NeuPink,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                NeuButton(
                                    onClick = { viewModel.openEditProduct(product) },
                                    backgroundColor = NeuYellow,
                                    shadowOffset = 2.dp,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeuBlack)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun StatMetricCard(icon: ImageVector, value: String, label: String, accentBg: Color) {
    NeuCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        shadowOffset = 4.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .background(accentBg, RoundedCornerShape(6.dp))
                    .border(1.5.dp, NeuBlack, RoundedCornerShape(6.dp))
                    .padding(5.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = NeuBlack,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = NeuBlack
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = NeuTextMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ───────────────────────────────────────────
// APP TAB: 2. PRODUCTS PAGE
// ───────────────────────────────────────────
@Composable
fun ProductsPage(viewModel: BusinessViewModel) {
    val products by viewModel.productsList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()

    val categories = listOf("all") + products.map { it.category }.distinct()

    val filteredProducts = products.filter { product ->
        val matchesSearch = product.name.contains(searchQuery, ignoreCase = true) ||
                product.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = categoryFilter == "all" || product.category == categoryFilter
        matchesSearch && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = "Kelola Produk",
                tint = NeuBlack,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Kelola Produk",
                style = MaterialTheme.typography.titleLarge,
                color = NeuBlack,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Input field
            BasicTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = NeuBlack),
                modifier = Modifier.weight(1f),
                decorationBox = @Composable { innerTextField ->
                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(3.dp, NeuBlack, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Cari", tint = NeuBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        if (searchQuery.isEmpty()) {
                            Text("Cari produk...", color = Color.Gray, fontSize = 14.sp)
                        }
                        innerTextField()
                    }
                }
            )

            NeuButton(
                onClick = { viewModel.openAddProduct() },
                backgroundColor = NeuYellow,
                shadowOffset = 3.dp,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.widthIn(max = 140.dp)
            ) {
                Text("+ Tambah", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isActive = categoryFilter == cat
                val chipBg = if (isActive) NeuYellow else Color.White
                Box(
                    modifier = Modifier
                        .background(chipBg, RoundedCornerShape(100.dp))
                        .border(2.dp, NeuBlack, RoundedCornerShape(100.dp))
                        .clickable { viewModel.setCategoryFilter(cat) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (cat == "all") "Semua" else "${getCategoryIcon(cat)} $cat",
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                        fontSize = 12.sp,
                        color = NeuBlack
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Product list
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Daftar Kosong",
                        tint = NeuTextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (products.isEmpty()) "Mulai tambahkan produk pertamamu!" else "Produk tidak ditemukan.",
                        fontSize = 14.sp,
                        color = NeuTextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredProducts, key = { it.id }) { item ->
                    val mp = if (item.sellingPrice > 0) ((item.sellingPrice - item.costPrice) / item.sellingPrice) * 100 else 0.0
                    val badgeColor = if (mp >= 60) NeuGreen else if (mp >= 30) NeuYellow else NeuPink
                    val diff = item.sellingPrice - item.costPrice

                    NeuCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White,
                        shadowOffset = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category icon container
                                Box(
                                    modifier = Modifier
                                        .background(NeuYellow.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                                        .size(42.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(getCategoryIcon(item.category), fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = NeuBlack,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.category} · Modal: ${formatRupiah(item.costPrice)} · Jual: ${formatRupiah(item.sellingPrice)}",
                                        fontSize = 11.sp,
                                        color = NeuTextMuted
                                    )
                                    Text(
                                        text = "Stok: ${item.stock} unit",
                                        fontSize = 11.sp,
                                        color = if (item.stock <= 10) NeuPink else NeuTextMuted,
                                        fontWeight = if (item.stock <= 10) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                // Margin % badge
                                Box(
                                    modifier = Modifier
                                        .background(badgeColor, RoundedCornerShape(8.dp))
                                        .border(1.5.dp, NeuBlack, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", mp)}%",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = NeuBlack
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "+${formatRupiahShort(diff)}/Unit",
                                    fontSize = 10.sp,
                                    color = NeuTextMuted,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Actions row
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = { viewModel.openEditProduct(item) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            modifier = Modifier.size(16.dp),
                                            tint = NeuBlack
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.requestDeleteProduct(item.id) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Hapus",
                                            modifier = Modifier.size(16.dp),
                                            tint = NeuPink
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ───────────────────────────────────────────
// APP TAB: 3. ANALYSIS PAGE
// ───────────────────────────────────────────
@Composable
fun AnalysisPage(viewModel: BusinessViewModel) {
    val products by viewModel.productsList.collectAsStateWithLifecycle()

    val sortedProducts = products.sortedByDescending { p ->
        if (p.sellingPrice > 0) ((p.sellingPrice - p.costPrice) / p.sellingPrice) * 100 else 0.0
    }

    var highMarginCount = 0
    var midMarginCount = 0
    var lowMarginCount = 0

    sortedProducts.forEach { p ->
        val mp = if (p.sellingPrice > 0) ((p.sellingPrice - p.costPrice) / p.sellingPrice) * 100 else 0.0
        if (mp >= 60) highMarginCount++
        else if (mp >= 30) midMarginCount++
        else lowMarginCount++
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = "Analisis Margin",
                tint = NeuBlack,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Analisis Margin",
                style = MaterialTheme.typography.titleLarge,
                color = NeuBlack,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Text(
            text = "Identifikasi produk paling menguntungkan",
            fontSize = 13.sp,
            color = NeuTextMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Distribution Summary
        NeuCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White
        ) {
            Column {
                Text(
                    text = "Distribusi Margin",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeuBlack
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DistributionBox("Tinggi", ">60%", highMarginCount.toString(), NeuGreen, modifier = Modifier.weight(1f))
                    DistributionBox("Sedang", "30-60%", midMarginCount.toString(), NeuOrange, modifier = Modifier.weight(1f))
                    DistributionBox("Rendah", "<30%", lowMarginCount.toString(), NeuPink, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Analysis Table List
        if (sortedProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Coba tambahkan produk dulu", color = NeuTextMuted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sortedProducts, key = { it.id }) { product ->
                    val mp = if (product.sellingPrice > 0) {
                        ((product.sellingPrice - product.costPrice) / product.sellingPrice) * 100
                    } else 0.0

                    val barColor = if (mp >= 60) NeuGreen else if (mp >= 30) NeuOrange else NeuPink

                    val recommendation = when {
                        mp < 30 -> "⬆️ Naikkan harga"
                        mp > 70 -> "⭐ Sangat menguntungkan"
                        product.stock <= 10 && mp >= 60 -> "🔄 Segera restock!"
                        else -> "✅ Pertahankan"
                    }

                    NeuCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White,
                        shadowOffset = 3.dp
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = NeuBlack
                                    )
                                    Text(
                                        text = "${getCategoryIcon(product.category)} ${product.category}",
                                        fontSize = 11.sp,
                                        color = NeuTextMuted
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = recommendation,
                                        fontSize = 11.sp,
                                        color = NeuBlack,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Modal: ${formatRupiahShort(product.costPrice)} | Jual: ${formatRupiahShort(product.sellingPrice)}",
                                        fontSize = 10.sp,
                                        color = NeuTextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom progress bar representation
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Real-looking Neubrutalist progress bar with rounded corners
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(14.dp)
                                        .background(Color(0xFFF5F5F5), RoundedCornerShape(100.dp))
                                        .border(2.dp, NeuBlack, RoundedCornerShape(100.dp))
                                ) {
                                    val pct = (mp.coerceIn(0.0, 100.0) / 100.0).toFloat()
                                    if (pct > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(pct)
                                                .background(barColor, RoundedCornerShape(100.dp))
                                                .border(1.5.dp, NeuBlack, RoundedCornerShape(100.dp))
                                        )
                                    }
                                }

                                Text(
                                    text = "${String.format("%.1f", mp)}%",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = barColor
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Margin: ${formatRupiah(product.sellingPrice - product.costPrice)}/unit",
                                fontSize = 11.sp,
                                color = NeuTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DistributionBox(label: String, range: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .border(2.dp, NeuBlack, RoundedCornerShape(12.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = NeuBlack,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = range,
                fontSize = 9.sp,
                color = NeuTextMuted
            )
        }
    }
}

// ───────────────────────────────────────────
// APP TAB: 4. STRATEGY (AI CENTER) PAGE
// ───────────────────────────────────────────
@Composable
fun StrategyPage(viewModel: BusinessViewModel) {
    val activeSubTab by viewModel.activeStrategyTab.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "Pusat Strategi",
                tint = NeuBlack,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Pusat Strategi",
                style = MaterialTheme.typography.titleLarge,
                color = NeuBlack,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Text(
            text = "Tools aksi nyata berbasis data bisnismu",
            fontSize = 13.sp,
            color = NeuTextMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal selections (Tips, WhatIf, Checklist, Recs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val subTabs = listOf(
                Triple("tips", "Tips", Icons.Default.Lightbulb),
                Triple("mentor", "Mentor AI", Icons.Default.Psychology),
                Triple("whatif", "Kalkulator", Icons.Default.Payments),
                Triple("checklist", "Aksi Harian", Icons.Default.DoneAll),
                Triple("recs", "Rekomendasi", Icons.Default.Star)
            )

            subTabs.forEach { tab ->
                val isActive = activeSubTab == tab.first
                val itemBg = if (isActive) NeuYellow else Color.White
                Box(
                    modifier = Modifier
                        .background(itemBg, RoundedCornerShape(100.dp))
                        .border(2.dp, NeuBlack, RoundedCornerShape(100.dp))
                        .clickable { viewModel.setStrategyTab(tab.first) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = tab.third,
                            contentDescription = tab.second,
                            tint = NeuBlack,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = tab.second,
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NeuBlack
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tab views
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                "tips" -> TipsSubTab(viewModel)
                "mentor" -> MentorSubTab(viewModel)
                "whatif" -> WhatIfSubTab(viewModel)
                "checklist" -> ChecklistSubTab(viewModel)
                "recs" -> RecommendationsSubTab(viewModel)
            }
        }
    }
}

// Sub Tab: Tips
@Composable
fun TipsSubTab(viewModel: BusinessViewModel) {
    val products by viewModel.productsList.collectAsStateWithLifecycle()

    val tips = remember(products) {
        buildDashboardTips(products)
    }

    if (tips.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tips akan muncul setelah kamu menambahkan produk", color = NeuTextMuted, fontSize = 13.sp)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(tips) { tip ->
                val vectorIcon = when (tip.icon) {
                    "💡" -> Icons.Default.Lightbulb
                    "⚠️" -> Icons.Default.Warning
                    "📈" -> Icons.Default.TrendingUp
                    "🔥" -> Icons.Default.Whatshot
                    else -> Icons.Default.Info
                }
                NeuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White,
                    shadowOffset = 4.dp
                ) {
                    Row {
                        Box(
                            modifier = Modifier
                                .background(NeuYellow.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .border(1.5.dp, NeuBlack, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = vectorIcon,
                                contentDescription = tip.title,
                                tint = NeuBlack,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(tip.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NeuBlack)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(tip.desc, fontSize = 12.sp, color = NeuTextMuted, lineHeight = 18.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .background(NeuYellow.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(1.5.dp, NeuBlack, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("→ ${tip.action}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NeuBlack)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Sub Tab: Mentor AI Chat
@Composable
fun MentorSubTab(viewModel: BusinessViewModel) {
    val messages by viewModel.mentorMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isMentorLoading.collectAsStateWithLifecycle()
    var inputQuery by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Scroll to latest message on loading/message count change
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 8.dp)
    ) {
        // Quick suggestion chips
        Text(
            text = "Saran Topik Tanya Mentor:",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = NeuTextMuted,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val suggestions = listOf(
                "Analisis profit & produk saya saat ini",
                "Bagaimana cara menaikkan profit margin?",
                "Strategi promosi untuk kategori bisnis saya",
                "Beri tips kelola stok agar tidak mati"
            )
            
            suggestions.forEach { suggest ->
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .border(1.5.dp, NeuBlack, RoundedCornerShape(20.dp))
                        .clickable {
                            inputQuery = suggest
                            viewModel.sendQuestionToMentor(suggest)
                            inputQuery = ""
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = suggest,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeuBlack
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat messages box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                .border(2.dp, NeuBlack, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    if (msg.isUser) {
                        // User message card alignment
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.5.dp, NeuBlack, RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Kakak (Saya)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NeuBlack
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = msg.message,
                                        fontSize = 12.sp,
                                        color = NeuBlack
                                    )
                                }
                            }
                        }
                    } else {
                        // Mentor message card alignment
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .background(NeuYellow.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .border(1.5.dp, NeuBlack, RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = "AI Mentor",
                                            tint = NeuBlack,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Mentor Bisnis AI",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = NeuBlack
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.message,
                                        fontSize = 12.sp,
                                        color = NeuBlack
                                    )
                                }
                            }
                        }
                    }
                }

                // Temporary typing / loading bubble
                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.5.dp, NeuBlack, RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = NeuYellow,
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Mentor sedang merumuskan jawaban...",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeuTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input chat field and submit button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(2.dp, NeuBlack, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = NeuBlack, fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (inputQuery.isEmpty()) {
                            Text(
                                text = "Tanya sesuatu ke Mentor AI...",
                                fontSize = 12.sp,
                                color = NeuTextMuted
                            )
                        }
                        innerTextField()
                    }
                )
            }

            NeuButton(
                onClick = {
                    if (inputQuery.isNotBlank() && !isLoading) {
                        viewModel.sendQuestionToMentor(inputQuery)
                        inputQuery = ""
                    }
                },
                backgroundColor = NeuYellow,
                modifier = Modifier.size(48.dp),
                shadowOffset = 2.dp
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Kirim",
                    tint = NeuBlack,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Sub Tab: What-If Calculator
@Composable
fun WhatIfSubTab(viewModel: BusinessViewModel) {
    val products by viewModel.productsList.collectAsStateWithLifecycle()
    val selectedId by viewModel.wifiSelectedProductId.collectAsStateWithLifecycle()
    val inputSellString by viewModel.wifiNewSellPrice.collectAsStateWithLifecycle()
    val inputCostString by viewModel.wifiNewCostPrice.collectAsStateWithLifecycle()

    var expandedSelect by remember { mutableStateOf(false) }

    val activeProduct = products.find { it.id == selectedId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NeuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White
            ) {
                Column {
                    Text(
                        text = "🧮 Simulasi: Kalau Harga Naik/Turun",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NeuBlack
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Selector Dropdown Box
                    Text(
                        text = "Pilih Produk",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NeuBlack,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(3.dp, NeuBlack, RoundedCornerShape(12.dp))
                            .clickable { expandedSelect = !expandedSelect }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeProduct?.name ?: "— Pilih produk —",
                                color = if (activeProduct != null) NeuBlack else Color.Gray,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (expandedSelect) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Pilih",
                                tint = NeuBlack
                            )
                        }

                        DropdownMenu(
                            expanded = expandedSelect,
                            onDismissRequest = { expandedSelect = false },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                        ) {
                            products.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.name, color = NeuBlack) },
                                    onClick = {
                                        viewModel.setWifiProduct(p.id)
                                        expandedSelect = false
                                    },
                                    modifier = Modifier.background(Color.White)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (activeProduct != null) {
                        // Current product analysis data card
                        val originalMp = if (activeProduct.sellingPrice > 0) {
                            ((activeProduct.sellingPrice - activeProduct.costPrice) / activeProduct.sellingPrice) * 100
                        } else 0.0

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                .border(1.5.dp, NeuBlack, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Data Sekarang",
                                    fontSize = 11.sp,
                                    color = NeuBlack,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(formatRupiah(activeProduct.costPrice), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Harga Modal", fontSize = 10.sp, color = NeuTextMuted)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(formatRupiah(activeProduct.sellingPrice), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Harga Jual", fontSize = 10.sp, color = NeuTextMuted)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${String.format("%.1f", originalMp)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeuYellow)
                                        Text("Margin Skrg", fontSize = 10.sp, color = NeuTextMuted)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${activeProduct.stock} unit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Stok", fontSize = 10.sp, color = NeuTextMuted)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Simulation interactive textfields
                        NeuTextField(
                            value = inputSellString,
                            onValueChange = { viewModel.updateWifiSellPrice(it) },
                            label = "Ubah Harga Jual Menjadi (Rp)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        NeuTextField(
                            value = inputCostString,
                            onValueChange = { viewModel.updateWifiCostPrice(it) },
                            label = "Ubah Harga Modal Menjadi (Rp)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Calculations output
                        val modSell = inputSellString.toDoubleOrNull() ?: activeProduct.sellingPrice
                        val modCost = inputCostString.toDoubleOrNull() ?: activeProduct.costPrice

                        if (modSell > 0 && modCost > 0) {
                            val newMarginPct = ((modSell - modCost) / modSell) * 100
                            val newMarginRp = modSell - modCost
                            val newTotalProfit = newMarginRp * activeProduct.stock
                            val oldTotalProfit = (activeProduct.sellingPrice - activeProduct.costPrice) * activeProduct.stock
                            val diff = newTotalProfit - oldTotalProfit

                            val resultClassColor = if (newMarginPct >= 60) NeuGreen else if (newMarginPct >= 30) NeuOrange else NeuPink

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                    .border(2.dp, NeuBlack, RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = "Hasil",
                                        tint = NeuTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "HASIL SIMULASI",
                                        fontSize = 11.sp,
                                        color = NeuTextMuted,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // box 1
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Margin Baru", fontSize = 9.sp, color = NeuTextMuted)
                                            Text("${String.format("%.1f", newMarginPct)}%", fontWeight = FontWeight.Black, fontSize = 14.sp, color = resultClassColor)
                                        }
                                    }
                                    // box 2
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Untung/Unit", fontSize = 9.sp, color = NeuTextMuted)
                                            Text(formatRupiahShort(newMarginRp), fontWeight = FontWeight.Black, fontSize = 14.sp, color = resultClassColor)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // box 3
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Total Profit Stok", fontSize = 9.sp, color = NeuTextMuted)
                                            Text(formatRupiahShort(newTotalProfit), fontWeight = FontWeight.Black, fontSize = 14.sp, color = resultClassColor)
                                        }
                                    }
                                    // box 4
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Selisih", fontSize = 9.sp, color = NeuTextMuted)
                                            Text(
                                                text = (if (diff >= 0) "+" else "") + formatRupiahShort(diff),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp,
                                                color = if (diff >= 0) NeuGreen else NeuPink
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Verdict card
                                val (verdictText, verdictBg, verdictTextClr) = when {
                                    newMarginPct < 0 -> Triple("❌ Harga jual di bawah modal — kamu rugi!", NeuPink.copy(alpha = 0.15f), NeuPink)
                                    newMarginPct < 20 -> Triple("⚠️ Margin terlalu tipis. Pertimbangkan naikkan harga.", NeuOrange.copy(alpha = 0.15f), NeuOrange)
                                    newMarginPct >= 50 -> Triple("🏆 Margin sangat sehat! Harga ini sangat direkomendasikan.", NeuGreen.copy(alpha = 0.15f), NeuGreen)
                                    else -> Triple("✅ Margin cukup baik. Kamu bisa lanjutkan dengan harga ini.", NeuBlue.copy(alpha = 0.15f), NeuBlue)
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(verdictBg, RoundedCornerShape(12.dp))
                                        .border(2.dp, NeuBlack, RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = verdictText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = NeuBlack,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Silakan pilih produk untuk memulai simulasi", fontSize = 13.sp, color = NeuTextMuted)
                        }
                    }
                }
            }
        }
    }
}

// Sub Tab: Checklist Aksi Harian
@Composable
fun ChecklistSubTab(viewModel: BusinessViewModel) {
    val products by viewModel.productsList.collectAsStateWithLifecycle()
    val checkedIds by viewModel.checkedItemIds.collectAsStateWithLifecycle()

    val contextItems = remember(products) {
        val items = mutableListOf<Pair<String, String>>()
        val lowStock = products.filter { it.stock <= 10 && it.stock > 0 }
        if (lowStock.isNotEmpty()) {
            items.add("ctx_stock" to "Segera restock: ${lowStock.take(3).joinToString { it.name }}")
        }
        val lowMargin = products.filter { p ->
            val mp = if (p.sellingPrice > 0) ((p.sellingPrice - p.costPrice) / p.sellingPrice) * 100 else 0.0
            mp < 25.0
        }
        if (lowMargin.isNotEmpty()) {
            items.add("ctx_margin" to "Review harga untuk: ${lowMargin.take(3).joinToString { it.name }}")
        }
        items
    }

    val staticChecklist = listOf(
        "c1" to "Cek stok produk yang hampir habis",
        "c2" to "Catat semua penjualan hari ini di OptimaBiz",
        "c3" to "Cek margin produk baru (jika ada penambahan)",
        "c4" to "Foto dan update konten produk di media sosial",
        "c5" to "Balas semua pesan/order pelanggan yang masuk",
        "c6" to "Review harga kompetitor untuk produk utama",
        "c7" to "Cek apakah ada produk yang perlu di-restock minggu ini",
        "c8" to "Hitung total pendapatan hari ini"
    )

    val displayedItems = contextItems + staticChecklist
    val totalItemsCount = displayedItems.size
    val checkedCount = displayedItems.count { checkedIds.contains(it.first) }

    val pctCompleted = if (totalItemsCount > 0) {
        (checkedCount.toFloat() / totalItemsCount * 100).toInt()
    } else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NeuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✅ Aksi Harian Bisnis",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NeuBlack
                        )
                        NeuButton(
                            onClick = { viewModel.resetChecklist() },
                            backgroundColor = NeuPink,
                            shadowOffset = 2.dp,
                            shape = RoundedCornerShape(8.dp),
                            fillMaxWidth = false,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeuBlack)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Centang aksi yang sudah kamu lakukan hari ini:",
                        fontSize = 12.sp,
                        color = NeuTextMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Progress hari ini", fontSize = 12.sp, color = NeuTextMuted)
                        Text("$pctCompleted%", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = NeuYellow)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    // Real Neubrutalist Progress scale
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(100.dp))
                            .border(2.dp, NeuBlack, RoundedCornerShape(100.dp))
                    ) {
                        val progressMultiplier = (pctCompleted / 100f).coerceIn(0f, 1f)
                        val barColor = if (pctCompleted >= 80) NeuGreen else if (pctCompleted >= 50) NeuOrange else NeuYellow
                        if (progressMultiplier > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressMultiplier)
                                    .background(barColor, RoundedCornerShape(100.dp))
                                    .border(1.5.dp, NeuBlack, RoundedCornerShape(100.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Checklist List items
                    displayedItems.forEach { item ->
                        val isChecked = checkedIds.contains(item.first)
                        val isContext = item.first.startsWith("ctx_")

                        val shape = RoundedCornerShape(8.dp)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(if (isChecked) NeuGreen.copy(alpha = 0.08f) else if (isContext) NeuPink.copy(alpha = 0.05f) else Color.White, shape)
                                .border(1.5.dp, if (isChecked) NeuGreen else if (isContext) NeuPink else NeuBlack, shape)
                                .clickable { viewModel.toggleChecklistItem(item.first) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Custom Neubrutalist checkbox box with rounded corner
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(if (isChecked) NeuGreen else Color.White, RoundedCornerShape(6.dp))
                                    .border(2.dp, NeuBlack, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChecked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selesai",
                                        tint = NeuBlack,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item.second,
                                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (isChecked) NeuTextMuted else NeuBlack,
                                fontSize = 13.sp,
                                fontWeight = if (isContext) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Sub Tab: Recommendations list
@Composable
fun RecommendationsSubTab(viewModel: BusinessViewModel) {
    val products by viewModel.productsList.collectAsStateWithLifecycle()
    val completedIds by viewModel.completedRecIds.collectAsStateWithLifecycle()
    val dismissedIds by viewModel.dismissedRecIds.collectAsStateWithLifecycle()

    val recommendations = remember(products, completedIds, dismissedIds) {
        val recs = mutableListOf<PromoRecommendation>()
        if (products.isEmpty()) return@remember recs

        products.forEach { p ->
            val mp = if (p.sellingPrice > 0) ((p.sellingPrice - p.costPrice) / p.sellingPrice) * 100 else 0.0

            // Option 1: repricing logic
            if (mp < 30.0) {
                val recommendedIncrement = Math.ceil((30.0 - mp) / 10.0) * 10.0
                val recommendedSellPrice = Math.ceil(p.sellingPrice * (1.0 + recommendedIncrement / 100.0))
                val profitPotential = (recommendedSellPrice - p.sellingPrice) * p.stock
                val id = "rec_pricing_${p.id}"
                if (!completedIds.contains(id) && !dismissedIds.contains(id)) {
                    recs.add(
                        PromoRecommendation(
                            id = id,
                            type = "PRICING",
                            emoji = "💰",
                            title = "Naikkan harga \"${p.name}\"",
                            desc = "Margin saat ini hanya ${String.format("%.1f", mp)}%. Coba naikkan harga jual ~${recommendedIncrement.toInt()}% dari ${formatRupiah(p.sellingPrice)} menjadi ${formatRupiah(recommendedSellPrice)} untuk margin yang jauh lebih sehat.",
                            impact = "+${formatRupiahShort(profitPotential)} potensi tambahan profit"
                        )
                    )
                }
            }

            // Option 2: stock review logic
            if (p.stock <= 5 && mp >= 50.0) {
                val id = "rec_restock_${p.id}"
                if (!completedIds.contains(id) && !dismissedIds.contains(id)) {
                    recs.add(
                        PromoRecommendation(
                            id = id,
                            type = "RESTOCK",
                            emoji = "🔄",
                            title = "Segera restock \"${p.name}\"",
                            desc = "Stok hampir habis (${p.stock} unit) sedangkan margin produk ini sangat menggiurkan yaitu ${String.format("%.1f", mp)}%. Jangan sampai kehilangan potensi penjualan harian!",
                            impact = "Amankan potensi profit optimal Anda"
                        )
                    )
                }
            }
        }

        // Option 3: focus strategy star product
        val star = products.sortedByDescending { if (it.sellingPrice > 0) ((it.sellingPrice - it.costPrice) / it.sellingPrice) * 100 else 0.0 }.firstOrNull()
        if (star != null) {
            val id = "rec_focus_all"
            val starMargin = if (star.sellingPrice > 0) ((star.sellingPrice - star.costPrice) / star.sellingPrice) * 100 else 0.0
            if (!completedIds.contains(id) && !dismissedIds.contains(id)) {
                recs.add(
                    PromoRecommendation(
                        id = id,
                        type = "FOCUS",
                        emoji = "🎯",
                        title = "Fokus promosi ke \"${star.name}\"",
                        desc = "Produk ini merupakan pencetak margin terbaik Anda sebesar ${String.format("%.1f", starMargin)}%. Alokasikan budget promosi mingguan lebih banyak ke item istimewa ini.",
                        impact = "Maksimalkan Return on Investment (ROI) modal"
                    )
                )
            }
        }

        // Option 4: bundle SUGGESTIONS
        val bundleCandidates = products.filter { p ->
            val mp = if (p.sellingPrice > 0) ((p.sellingPrice - p.costPrice) / p.sellingPrice) * 100 else 0.0
            mp >= 50.0
        }
        if (bundleCandidates.size >= 2) {
            val id = "rec_bundle_suggest"
            if (!completedIds.contains(id) && !dismissedIds.contains(id)) {
                recs.add(
                    PromoRecommendation(
                        id = id,
                        type = "BUNDLE",
                        emoji = "🎁",
                        title = "Buat paket bundle produk unggulan",
                        desc = "Jual bersamaan \"${bundleCandidates[0].name}\" + \"${bundleCandidates[1].name}\" sebagai kesatuan paket diskon. Strategi bundle idealnya mendongkrak average order value transaksi hingga 30%.",
                        impact = "Naikkan rata-rata nilai order +20-30%"
                    )
                )
            }
        }

        recs
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Berdasarkan data produkmu:", fontSize = 12.sp, color = NeuTextMuted)
            Text(
                "Reset Semua",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NeuBlue,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { viewModel.resetRecommendations() }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (recommendations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Prima",
                        tint = NeuGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bisnis kamu sudah dalam kondisi prima!\nSemua target tercapai dan tidak ada hambatan.",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = NeuTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(recommendations, key = { it.id }) { rec ->
                    val typeBg = when (rec.type) {
                        "PRICING" -> NeuBlue
                        "RESTOCK" -> NeuOrange
                        "FOCUS" -> NeuGreen
                        else -> NeuPurple
                    }
                    val typeIcon = when (rec.type) {
                        "PRICING" -> Icons.Default.MonetizationOn
                        "RESTOCK" -> Icons.Default.Autorenew
                        "FOCUS" -> Icons.Default.Star
                        else -> Icons.Default.Redeem
                    }

                    NeuCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White,
                        shadowOffset = 4.dp
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(typeBg, RoundedCornerShape(8.dp))
                                        .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = typeIcon,
                                            contentDescription = rec.type,
                                            tint = NeuBlack,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = rec.type,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            color = NeuBlack
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = rec.title,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = NeuBlack
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = rec.desc,
                                fontSize = 12.sp,
                                color = NeuTextMuted,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Impact badge with vector icon
                            Box(
                                modifier = Modifier
                                    .background(NeuGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(1.5.dp, NeuBlack, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = "Impact",
                                        tint = NeuGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = rec.impact,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeuBlack
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                NeuButton(
                                    onClick = { viewModel.markRecComplete(rec.id) },
                                    backgroundColor = NeuGreen,
                                    shadowOffset = 2.dp,
                                    modifier = Modifier.weight(1.5f)
                                ) {
                                    Text("Sudah Dilakukan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                NeuOutlineButton(
                                    onClick = { viewModel.dismissRec(rec.id) },
                                    shadowOffset = 2.dp,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Abaikan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ───────────────────────────────────────────
// APP TAB: 5. PROFILE PAGE
// ───────────────────────────────────────────
@Composable
fun ProfilePage(viewModel: BusinessViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val products by viewModel.productsList.collectAsStateWithLifecycle()

    val totalProducts = products.size
    val totalInventoryWorth = products.sumOf { it.stock * it.costPrice }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.updateProfilePhoto(it.toString())
        }
    }

    var bizName by remember { mutableStateOf(currentUser?.bizName ?: "") }
    var category by remember { mutableStateOf(currentUser?.category ?: "F&B") }
    var expandedCat by remember { mutableStateOf(false) }

    val categories = listOf("F&B", "Fashion", "Elektronik", "Kecantikan", "Kerajinan", "Lainnya")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profil",
                    tint = NeuBlack,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Profil Tokomu",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeuBlack,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Profile Header Card
            NeuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .background(NeuYellow, RoundedCornerShape(12.dp))
                                .border(3.dp, NeuBlack, RoundedCornerShape(12.dp))
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    imagePickerLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val photoUri = currentUser?.profileImageUri
                            if (!photoUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = photoUri,
                                    contentDescription = "Foto Profil Toko",
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(getCategoryIcon(currentUser?.category ?: "F&B"), fontSize = 32.sp)
                            }

                            // Camera floating tag
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(NeuBlack, RoundedCornerShape(topStart = 8.dp))
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Ganti Foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ganti Foto",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeuTextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = currentUser?.bizName ?: "Toko Kopi Maju",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = NeuBlack
                        )
                        Text(
                            text = currentUser?.email ?: "email@umkm.com",
                            fontSize = 12.sp,
                            color = NeuTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(NeuYellow.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.5.dp, NeuBlack, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Plan",
                                    tint = NeuBlack,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Free Plan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = NeuBlack
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            // Ringkasan Bisnis Card
            NeuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Ringkasan",
                            tint = NeuBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Ringkasan Bisnis",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NeuBlack
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                .border(1.5.dp, NeuBlack, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(totalProducts.toString(), fontWeight = FontWeight.Black, fontSize = 20.sp, color = NeuBlack)
                                Text("Total Produk", fontSize = 10.sp, color = NeuTextMuted)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                .border(1.5.dp, NeuBlack, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(formatRupiahShort(totalInventoryWorth), fontWeight = FontWeight.Black, fontSize = 20.sp, color = NeuGreen)
                                Text("Aset Inventaris", fontSize = 10.sp, color = NeuTextMuted)
                            }
                        }
                    }
                }
            }
        }

        item {
            // Edit profile settings
            NeuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White
            ) {
                Column {
                    Text(
                        text = "⚙️ Edit Profil Bisnis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NeuBlack
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    NeuTextField(
                        value = bizName,
                        onValueChange = { bizName = it },
                        label = "Nama UMKM",
                        placeholder = "Nama Toko Baru"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category edit dropdown
                    Column {
                        Text(
                            text = "Kategori Bisnis",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NeuTextMuted,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(3.dp, NeuBlack, RoundedCornerShape(12.dp))
                                .clickable { expandedCat = !expandedCat }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${getCategoryIcon(category)} $category",
                                    color = NeuBlack,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = if (expandedCat) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Pilih",
                                    tint = NeuBlack
                                )
                            }

                            DropdownMenu(
                                expanded = expandedCat,
                                onDismissRequest = { expandedCat = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text("${getCategoryIcon(cat)} $cat", color = NeuBlack) },
                                        onClick = {
                                            category = cat
                                            expandedCat = false
                                        },
                                        modifier = Modifier.background(Color.White)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    NeuButton(
                        onClick = { viewModel.updateProfile(bizName, category) },
                        backgroundColor = NeuYellow
                    ) {
                        Text("Simpan Perubahan", fontWeight = FontWeight.Bold, color = NeuBlack)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            NeuButton(
                onClick = { viewModel.handleLogout() },
                backgroundColor = NeuPink,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🚪 Keluar dari Akun", fontWeight = FontWeight.Bold, color = NeuBlack)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ============================================================
// DIALOG OVERLAYS IMPLEMENTATION
// ============================================================

@Composable
fun ProductEditDialog(viewModel: BusinessViewModel) {
    val editingProduct by viewModel.editingProduct.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf(editingProduct?.name ?: "") }
    var category by remember { mutableStateOf(editingProduct?.category ?: "Makanan") }
    var initialStockString by remember { mutableStateOf(editingProduct?.stock?.toString() ?: "") }
    var costPriceString by remember { mutableStateOf(editingProduct?.costPrice?.toInt()?.toString() ?: "") }
    var sellingPriceString by remember { mutableStateOf(editingProduct?.sellingPrice?.toInt()?.toString() ?: "") }
    var description by remember { mutableStateOf(editingProduct?.description ?: "") }

    var expandedCat by remember { mutableStateOf(false) }
    val categories = listOf("Makanan", "Minuman", "Fashion", "Elektronik", "Kecantikan", "Kerajinan", "Lainnya")

    Dialog(
        onDismissRequest = { viewModel.closeProductDialog() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { viewModel.closeProductDialog() },
            contentAlignment = Alignment.Center
        ) {
            // Main Overlay Dialog Box
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .verticalScroll(rememberScrollState())
                    .clickable(enabled = false) {} // block click propagation
                    .background(Color(0xFFFFFDF5), RoundedCornerShape(16.dp))
                    .border(3.dp, NeuBlack, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingProduct == null) "TAMBAH PRODUK" else "EDIT PRODUK",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = NeuBlack
                    )
                    IconButton(
                        onClick = { viewModel.closeProductDialog() },
                        modifier = Modifier
                            .size(32.dp)
                            .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                            .background(Color.White, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = NeuBlack, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                NeuTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nama Produk *",
                    placeholder = "Contoh: Kopi Arabika Premium"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category drop select
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = "Kategori",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NeuBlack,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(3.dp, NeuBlack, RoundedCornerShape(12.dp))
                                .clickable { expandedCat = !expandedCat }
                                .padding(horizontal = 12.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category,
                                    color = NeuBlack,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = if (expandedCat) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Pilih",
                                    tint = NeuBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = expandedCat,
                                onDismissRequest = { expandedCat = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .border(2.dp, NeuBlack, RoundedCornerShape(8.dp))
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, color = NeuBlack) },
                                        onClick = {
                                            category = cat
                                            expandedCat = false
                                        },
                                        modifier = Modifier.background(Color.White)
                                    )
                                }
                            }
                        }
                    }

                    // Stock input
                    NeuTextField(
                        value = initialStockString,
                        onValueChange = { initialStockString = it },
                        label = "Stok Awal",
                        placeholder = "0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeuTextField(
                        value = costPriceString,
                        onValueChange = { costPriceString = it },
                        label = "Harga Modal (Rp) *",
                        placeholder = "5000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    NeuTextField(
                        value = sellingPriceString,
                        onValueChange = { sellingPriceString = it },
                        label = "Harga Jual (Rp) *",
                        placeholder = "15000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                NeuTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Deskripsi (opsional)",
                    placeholder = "Tulis detail deskripsi produk..."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time margin preview
                val cost = costPriceString.toDoubleOrNull() ?: 0.0
                val sell = sellingPriceString.toDoubleOrNull() ?: 0.0
                if (cost > 0 && sell > 0) {
                    val marginRp = sell - cost
                    val marginPct = if (sell > 0) (marginRp / sell) * 100 else 0.0
                    val textClr = if (marginPct >= 60) NeuGreen else if (marginPct >= 30) NeuOrange else NeuPink

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                            .border(2.dp, NeuBlack, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Preview Margin", fontSize = 10.sp, color = NeuTextMuted, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column {
                                    Text(formatRupiah(marginRp), fontSize = 15.sp, fontWeight = FontWeight.Black, color = textClr)
                                    Text("Margin per unit", fontSize = 10.sp, color = NeuTextMuted)
                                }
                                Column {
                                    Text("${String.format("%.1f", marginPct)}%", fontSize = 15.sp, fontWeight = FontWeight.Black, color = textClr)
                                    Text("Persentase margin", fontSize = 10.sp, color = NeuTextMuted)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Call buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeuOutlineButton(
                        onClick = { viewModel.closeProductDialog() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", fontWeight = FontWeight.Bold, color = NeuBlack)
                    }

                    NeuButton(
                        onClick = {
                            viewModel.saveProduct(
                                name = name,
                                category = category,
                                costPrice = cost,
                                sellingPrice = sell,
                                stock = initialStockString.toIntOrNull() ?: 0,
                                description = description
                            )
                        },
                        backgroundColor = NeuYellow,
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold, color = NeuBlack)
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(viewModel: BusinessViewModel) {
    Dialog(onDismissRequest = { viewModel.cancelDeleteProduct() }) {
        Column(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(3.dp, NeuBlack, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "HAPUS PRODUK?",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = NeuBlack
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Produk ini akan dihapus permanen dan tidak bisa dikembalikan.",
                fontSize = 13.sp,
                color = NeuTextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeuOutlineButton(
                    onClick = { viewModel.cancelDeleteProduct() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }
                NeuButton(
                    onClick = { viewModel.confirmDeleteProduct() },
                    backgroundColor = NeuPink,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold, color = NeuBlack)
                }
            }
        }
    }
}

// ============================================================
// DATA MODELS & HELPER STRUCTURES
// ============================================================

data class DashboardTip(
    val id: String,
    val icon: String,
    val title: String,
    val desc: String,
    val action: String
)

data class PromoRecommendation(
    val id: String,
    val type: String,
    val emoji: String,
    val title: String,
    val desc: String,
    val impact: String
)

fun buildDashboardTips(products: List<Product>): List<DashboardTip> {
    val tips = mutableListOf<DashboardTip>()
    
    // Tip 1: Base general tip
    tips.add(
        DashboardTip(
            id = "tip_general",
            icon = "💡",
            title = "Optimasi Margin Profit",
            desc = "Usahakan margin rata-rata tokomu berada di atas 30% untuk menutup biaya operasional dan pemasaran.",
            action = "Cek Menu Analisis"
        )
    )

    // Tip 2: If there are low-stock products
    val lowStock = products.filter { it.stock <= 10 }
    if (lowStock.isNotEmpty()) {
        tips.add(
            DashboardTip(
                id = "tip_restock",
                icon = "⚠️",
                title = "Stok Kritis Terdeteksi",
                desc = "Ada ${lowStock.size} produk dengan stok di bawah 10 unit. Segera lakukan pemesanan ulang agar pelanggan tidak kecewa.",
                action = "Hubungi Supplier"
            )
        )
    }

    // Tip 3: If there are products with low margin
    val lowMargin = products.filter { p ->
        val mp = if (p.sellingPrice > 0) ((p.sellingPrice - p.costPrice) / p.sellingPrice) * 100 else 0.0
        mp < 25.0
    }
    if (lowMargin.isNotEmpty()) {
        tips.add(
            DashboardTip(
                id = "tip_reprice",
                icon = "📈",
                title = "Naikkan Kelas Profit",
                desc = "${lowMargin.size} produk tokomu memiliki margin di bawah 25%. Coba review kembali harga beli dari supplier atau naikkan sedikit harga jual.",
                action = "Simulasikan Harga"
            )
        )
    }

    // Tip 4: If there are strong high margin products
    val highMargin = products.filter { p ->
        val mp = if (p.sellingPrice > 0) ((p.sellingPrice - p.costPrice) / p.sellingPrice) * 100 else 0.0
        mp >= 50.0
    }
    if (highMargin.isNotEmpty()) {
        tips.add(
            DashboardTip(
                id = "tip_promote",
                icon = "🔥",
                title = "Fokus Produk Unggulan",
                desc = "Kamu punya ${highMargin.size} produk dengan margin super sehat (>50%). Jadikan produk ini sebagai menu rekomendasi utama atau pasang di etalase depan toko.",
                action = "Buat Promo Bundle"
            )
        )
    }

    return tips
}

// ============================================================
// FORMATTERS & STATIC HELPERS
// ============================================================

fun formatRupiah(value: Double): String {
    return "Rp " + String.format(Locale("id", "ID"), "%,.0f", value)
}

fun formatRupiahShort(value: Double): String {
    return when {
        value >= 1_000_000_000 -> "Rp " + String.format("%.1f", value / 1_000_000_000.0) + " M"
        value >= 1_000_000 -> "Rp " + String.format("%.1f", value / 1_000_000.0) + " jt"
        value >= 1_000 -> "Rp " + String.format("%.0f", value / 1_000.0) + " rb"
        else -> formatRupiah(value)
    }
}

fun getCategoryIconVector(cat: String): ImageVector {
    return when (cat) {
        "Makanan" -> Icons.Default.Restaurant
        "Minuman", "F&B" -> Icons.Default.LocalCafe
        "Fashion" -> Icons.Default.Checkroom
        "Elektronik" -> Icons.Default.Devices
        "Kecantikan" -> Icons.Default.Spa
        "Kerajinan" -> Icons.Default.Palette
        else -> Icons.Default.Category
    }
}

fun getCategoryIcon(cat: String): String {
    return ""
}
