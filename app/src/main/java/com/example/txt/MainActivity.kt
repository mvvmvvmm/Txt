package com.example.txt

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.txt.ui.theme.GrayLight
import com.example.txt.ui.theme.TextPrimary
import com.example.txt.ui.theme.TxtTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TxtTheme {
                FileViewerScreen()
            }
        }
    }
}

@Composable
fun FileViewerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var contentText by remember { mutableStateOf("Содержимое файла появится здесь...") }
    var isError by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            isLoading = false
            return@rememberLauncherForActivityResult
        }

        isLoading = true
        isError = false
        contentText = ""

        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver
                        .openInputStream(uri)
                        ?.use { it.bufferedReader().readText() }
                        ?: throw IllegalStateException("Не удалось открыть поток файла")
                }
            }

            isLoading = false

            result.onSuccess { text ->
                if (text.isBlank()) {
                    isError = true
                    contentText = "Ошибка: файл пуст или содержит только пробелы."
                } else {
                    isError = false
                    contentText = text
                }
            }.onFailure { e ->
                isError = true
                contentText = "Ошибка чтения файла: ${e.localizedMessage ?: "неизвестная ошибка"}"
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Button(
                onClick = {
                    isLoading = true
                    launcher.launch(arrayOf("text/plain"))
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Открыть файл", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = GrayLight,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = contentText,
                    fontSize = 16.sp,
                    color = if (isError) MaterialTheme.colorScheme.error else TextPrimary,
                    fontWeight = if (isError) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}