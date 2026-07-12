package com.prog7313.sandbox.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.prog7313.sandbox.model.openlibrary.OpenLibraryBookDto
import com.prog7313.sandbox.network.openlibrary.OpenLibraryRepository
import kotlinx.coroutines.CancellationException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private sealed interface OpenLibraryUiState {

    data object Idle : OpenLibraryUiState

    data object Loading : OpenLibraryUiState

    data class Success(
        val totalResults: Int,
        val books: List<OpenLibraryBookDto>
    ) : OpenLibraryUiState

    data class Error(
        val message: String
    ) : OpenLibraryUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenLibraryDemoScreen(
    onBack: () -> Unit
) {
    var query by remember {
        mutableStateOf("")
    }

    var submittedQuery by remember {
        mutableStateOf("")
    }

    var requestNumber by remember {
        mutableIntStateOf(0)
    }

    var validationMessage by remember {
        mutableStateOf<String?>(null)
    }

    var uiState by remember {
        mutableStateOf<OpenLibraryUiState>(
            OpenLibraryUiState.Idle
        )
    }

    fun search() {
        val cleanQuery = query.trim()

        if (cleanQuery.isBlank()) {
            validationMessage =
                "Enter a title, author or keyword."

            return
        }

        validationMessage = null
        submittedQuery = cleanQuery
        requestNumber++
    }

    LaunchedEffect(
        submittedQuery,
        requestNumber
    ) {
        if (submittedQuery.isBlank()) {
            return@LaunchedEffect
        }

        uiState = OpenLibraryUiState.Loading

        try {
            val response =
                OpenLibraryRepository.searchBooks(
                    query = submittedQuery
                )

            uiState = OpenLibraryUiState.Success(
                totalResults = response.numberFound,
                books = response.docs
            )
        } catch (error: Exception) {
            if (error is CancellationException) {
                throw error
            }

            uiState = OpenLibraryUiState.Error(
                message = error.toFriendlyMessage()
            )
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Search Open Library",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text =
                "The value entered below is passed to Retrofit " +
                        "as the q query parameter."
        )

        OutlinedTextField(
            value = query,
            onValueChange = { newValue ->
                query = newValue
                validationMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Search books")
            },
            placeholder = {
                Text("Title, author or keyword")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null
                )
            },
            singleLine = true,
            isError = validationMessage != null,
            supportingText =
                validationMessage?.let { message ->
                    {
                        Text(message)
                    }
                },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    search()
                }
            )
        )

        Button(
            onClick = {
                search()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled =
                uiState !is OpenLibraryUiState.Loading
        ) {
            Text("Search")
        }

        when (val state = uiState) {
            OpenLibraryUiState.Idle -> {
                IdleContent()
            }

            OpenLibraryUiState.Loading -> {
                LoadingContent()
            }

            is OpenLibraryUiState.Success -> {
                SuccessContent(
                    query = submittedQuery,
                    totalResults = state.totalResults,
                    books = state.books
                )
            }

            is OpenLibraryUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = {
                        requestNumber++
                    }
                )
            }
        }
    }
}

@Composable
private fun IdleContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter a search term to make the API request.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()

        Text(
            text = "Requesting books...",
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun SuccessContent(
    query: String,
    totalResults: Int,
    books: List<OpenLibraryBookDto>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "$totalResults results for \"$query\"",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (books.isEmpty()) {
            item {
                Text(
                    text = "No books found."
                )
            }
        } else {
            items(
                items = books,
                key = { book ->
                    book.key
                }
            ) { book ->
                BookResultCard(
                    book = book
                )
            }
        }
    }
}

@Composable
private fun BookResultCard(
    book: OpenLibraryBookDto
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = if (book.authorNames.isEmpty()) {
                    "Unknown author"
                } else {
                    book.authorNames.joinToString()
                }
            )

            Text(
                text = book.firstPublishYear?.let { year ->
                    "First published: $year"
                } ?: "Publication year unavailable",
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Request failed",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = message,
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 16.dp
            )
        )

        Button(
            onClick = onRetry
        ) {
            Text("Retry")
        }
    }
}

private fun Throwable.toFriendlyMessage(): String {
    return when (this) {
        is UnknownHostException ->
            "No internet connection."

        is SocketTimeoutException ->
            "The request took too long."

        else ->
            message ?: "Something went wrong."
    }
}