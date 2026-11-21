package com.example.txt

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Класс для хранения состояния экрана
data class FileUiState(
    val isLoading: Boolean = false,
    val contentText: String = "Содержимое файла появится здесь...",
    val isError: Boolean = false
)

class FileReaderViewModel(application: Application) : AndroidViewModel(application) {

    // Приватное изменяемое состояние
    private val _uiState = MutableStateFlow(FileUiState())
    // Публичное состояние только для чтения (для UI)
    val uiState: StateFlow<FileUiState> = _uiState.asStateFlow()

    // Функция чтения файла
    fun readFile(uri: Uri) {
        // Включаем индикатор загрузки и очищаем предыдущий текст
        _uiState.value = FileUiState(isLoading = true, contentText = "")

        // Запускаем корутину
        viewModelScope.launch {
            // Читаем файл в фоновом потоке
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    getApplication<Application>().contentResolver
                        .openInputStream(uri)
                        ?.use { it.bufferedReader().readText() }
                        ?: throw IllegalStateException("Не удалось открыть поток файла")
                }
            }

            // Обрабатываем результат в главном потоке
            result.onSuccess { text ->
                if (text.isBlank()) {
                    _uiState.value = FileUiState(
                        isLoading = false,
                        contentText = "Ошибка: файл пуст или содержит только пробелы.",
                        isError = true
                    )
                } else {
                    _uiState.value = FileUiState(
                        isLoading = false,
                        contentText = text,
                        isError = false
                    )
                }
            }.onFailure { e ->
                _uiState.value = FileUiState(
                    isLoading = false,
                    contentText = "Ошибка чтения файла: ${e.localizedMessage ?: "неизвестная ошибка"}",
                    isError = true
                )
            }
        }
    }

    // Функция для сброса состояния при отмене выбора файла
    fun cancelLoading() {
        _uiState.value = _uiState.value.copy(isLoading = false)
    }
}