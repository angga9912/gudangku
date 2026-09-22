package com.gudangku.app.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gudangku.app.data.entity.TransactionType
import com.gudangku.app.ui.components.EmptyState
import com.gudangku.app.ui.components.formatDateTime
import com.gudangku.app.viewmodel.HistoryFilter
import com.gudangku.app.viewmodel.TransactionViewModel
import com.gudangku.app.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(factory: ViewModelFactory) {
    val viewModel: TransactionViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Riwayat Transaksi") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Cari nama barang atau SKU...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HistoryFilter.entries.toList()) { filter ->
                    FilterChip(
                        selected = uiState.filter == filter,
                        onClick = { viewModel.onFilterChange(filter) },
                        label = { Text(filter.label) }
                    )
                }
            }

            if (uiState.transactions.isEmpty() && !uiState.isLoading) {
                EmptyState(message = "Belum ada transaksi yang cocok.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.transactions, key = { it.transactionId }) { tx ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = tx.productName, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (tx.transactionType == TransactionType.STOK_MASUK) "Stok Masuk" else "Stok Keluar",
                                        color = if (tx.transactionType == TransactionType.STOK_MASUK)
                                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                    Text(text = formatDateTime(tx.date), style = MaterialTheme.typography.bodyMedium)
                                    if (!tx.note.isNullOrBlank()) {
                                        Text(text = tx.note, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                                Text(
                                    text = "${if (tx.transactionType == TransactionType.STOK_MASUK) "+" else "-"}${tx.quantity}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
