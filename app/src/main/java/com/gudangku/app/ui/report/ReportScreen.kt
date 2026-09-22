package com.gudangku.app.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import com.gudangku.app.ui.components.LowStockBadge
import com.gudangku.app.ui.components.StatCard
import com.gudangku.app.ui.components.formatDateTime
import com.gudangku.app.viewmodel.ReportPeriod
import com.gudangku.app.viewmodel.ReportViewModel
import com.gudangku.app.viewmodel.ViewModelFactory

@Composable
fun ReportScreen(factory: ViewModelFactory) {
    val viewModel: ReportViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Laporan") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ReportPeriod.entries.toList()) { period ->
                        FilterChip(
                            selected = uiState.period == period,
                            onClick = { viewModel.onPeriodChange(period) },
                            label = { Text(period.label) }
                        )
                    }
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(title = "Total Stok", value = "${uiState.totalStock}", modifier = Modifier.weight(1f))
                    StatCard(
                        title = "Total Stok Masuk",
                        value = "${uiState.totalStockIn}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                StatCard(
                    title = "Total Stok Keluar",
                    value = "${uiState.totalStockOut}",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    text = "Barang Stok Menipis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (uiState.lowStockProducts.isEmpty()) {
                item { Text("Tidak ada barang dengan stok menipis.", color = MaterialTheme.colorScheme.outline) }
            } else {
                items(uiState.lowStockProducts, key = { it.productId }) { p ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = p.name, fontWeight = FontWeight.Bold)
                            Text(text = "Stok: ${p.currentStock} ${p.unit} (min ${p.minStock})")
                            LowStockBadge()
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Riwayat Transaksi Periode Ini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (uiState.transactions.isEmpty()) {
                item { EmptyState(message = "Tidak ada transaksi pada periode ini.") }
            } else {
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
                                    text = if (tx.transactionType == TransactionType.STOK_MASUK) "Stok Masuk" else "Stok Keluar"
                                )
                                Text(text = formatDateTime(tx.date), style = MaterialTheme.typography.bodyMedium)
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
