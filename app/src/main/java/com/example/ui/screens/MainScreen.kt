package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.FpsGold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SampleGameLibrary
import com.example.model.XboxGame
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DimensityCyan
import com.example.ui.theme.MaliPurple
import com.example.ui.theme.XenGreen

import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

enum class XenTab(val title: String, val icon: ImageVector) {
    LIBRARY("Biblioteca", Icons.Default.SportsEsports),
    EMULATOR("Emulador", Icons.Default.Gamepad),
    DIMENSITY_MALI("Dimensity/Mali", Icons.Default.ElectricBolt),
    CPP_PATCHES("Código C++", Icons.Default.Code)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf(XenTab.LIBRARY) }
    var activeGame by remember { mutableStateOf(SampleGameLibrary.games.first()) }
    var showInstallDialog by remember { mutableStateOf(false) }

    // Back handling: return to Library if on any other tab
    BackHandler(enabled = currentTab != XenTab.LIBRARY) {
        currentTab = XenTab.LIBRARY
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Brush.linearGradient(listOf(XenGreen, DimensityCyan))),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text("X", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "XenDroid",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Dimensity & Mali Edition",
                                color = DimensityCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    Button(
                        onClick = { showInstallDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = XenGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp).height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Instalar APK",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B101C)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0C121E),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .testTag("main_navigation_bar")
            ) {
                XenTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = XenGreen,
                            indicatorColor = when (tab) {
                                XenTab.DIMENSITY_MALI -> DimensityCyan
                                XenTab.CPP_PATCHES -> MaliPurple
                                else -> XenGreen
                            },
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                XenTab.LIBRARY -> {
                    LibraryScreen(
                        onSelectGame = { game ->
                            activeGame = game
                            currentTab = XenTab.EMULATOR
                        }
                    )
                }
                XenTab.EMULATOR -> {
                    EmulatorScreen(
                        selectedGame = activeGame,
                        onNavigateToDimensitySettings = {
                            currentTab = XenTab.DIMENSITY_MALI
                        }
                    )
                }
                XenTab.DIMENSITY_MALI -> {
                    DimensityMaliScreen()
                }
                XenTab.CPP_PATCHES -> {
                    CppPatchesScreen()
                }
            }

            if (showInstallDialog) {
                InstallGuideDialog(onDismiss = { showInstallDialog = false })
            }
        }
    }
}

@Composable
private fun InstallGuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = XenGreen,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Cómo Instalar en tu Teléfono",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Sigue estos 4 pasos sencillos para tener este APK ejecutándose en tu dispositivo Android real:",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                // Paso 1
                InstallStepCard(
                    stepNumber = "1",
                    title = "Descarga el archivo APK",
                    description = "En la barra superior o menú de configuración de Google AI Studio (arriba a la derecha), haz clic en el icono de descarga o 'Download APK' para guardar el archivo .apk generado.",
                    accentColor = XenGreen
                )

                // Paso 2
                InstallStepCard(
                    stepNumber = "2",
                    title = "Transfiere o abre el APK",
                    description = "Si descargaste desde tu teléfono, encuéntralo en tu carpeta de 'Descargas'. Si estás en PC, envíatelo por WhatsApp Web, Telegram, Google Drive o cable USB.",
                    accentColor = DimensityCyan
                )

                // Paso 3
                InstallStepCard(
                    stepNumber = "3",
                    title = "Habilita Orígenes Desconocidos",
                    description = "Al tocar el archivo APK, Android te pedirá autorización: Pulsa 'Ajustes' y activa el interruptor 'Permitir desde esta fuente' para tu navegador o explorador de archivos.",
                    accentColor = FpsGold
                )

                // Paso 4
                InstallStepCard(
                    stepNumber = "4",
                    title = "Play Protect (App de Desarrollo)",
                    description = "Al ser una compilación de desarrollo firmada con debug key, si Play Protect muestra 'App no verificada', pulsa en 'Más detalles' y luego en 'Instalar de todas formas'.",
                    accentColor = MaliPurple
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = XenGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("¡Entendido!", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun InstallStepCard(
    stepNumber: String,
    title: String,
    description: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131D31), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = androidx.compose.ui.Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(accentColor),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
