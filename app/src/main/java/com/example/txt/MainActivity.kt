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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.txt.ui.theme.GrayLight
import com.example.txt.ui.theme.TextPrimary
import com.example.txt.ui.theme.TxtTheme

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
fun FileViewerScreen(viewModel: FileReaderViewModel = viewModel()) {
    // Подписываемся на состояние из ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Лаунчер для вызова системного файлового менеджера
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            viewModel.cancelLoading()
        } else {
            viewModel.readFile(uri) // Передаем файл в логику
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
            // КНОПКА
            Button(
                onClick = {
                    launcher.launch(arrayOf("text/plain"))
                },
                enabled = !uiState.isLoading, // Блокируем, если идет загрузка
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Открыть файл", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ИНДИКАТОР
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ОБЛАСТЬ ТЕКСТА
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(color = GrayLight, shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = uiState.contentText,
                    fontSize = 16.sp,
                    color = if (uiState.isError) MaterialTheme.colorScheme.error else TextPrimary,
                    fontWeight = if (uiState.isError) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}