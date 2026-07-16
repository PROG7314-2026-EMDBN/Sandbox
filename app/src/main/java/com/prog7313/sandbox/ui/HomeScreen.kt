package com.prog7313.sandbox.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun HomeScreen(
    onOpenForm: () -> Unit,
    onOpenGadgets: () -> Unit,
    onOpenFocusLog: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenLocationDemo: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sandbox Home", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(20.dp))
        AssetImage("code.jpg")
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onOpenForm,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Open Form") }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onOpenGadgets,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Open Gadgets") }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onOpenFocusLog,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Open Focus Log") }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onOpenNotifications,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Open Notifications Demo") }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onOpenProfile,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Open Profile") }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onOpenLibrary,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Retrofit API Demo") }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = onOpenLocationDemo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Location Access Demo")
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Exit") }
    }
}

@Composable
fun AssetImage(imageName: String) {
    val context = LocalContext.current

    val bitmap = remember(imageName) {
        context.assets.open("images/$imageName").use { input ->
            BitmapFactory.decodeStream(input)
        }
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = imageName,
        modifier = Modifier
            .fillMaxWidth(0.50f)
            .height(250.dp),
        contentScale = ContentScale.Fit
    )
}
