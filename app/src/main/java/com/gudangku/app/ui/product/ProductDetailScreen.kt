package com.gudangku.app.ui.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gudangku.app.data.entity.TransactionType
import com.gudangku.app.data.repository.OperationResult
import com.gudangku.app.ui.components.ConfirmDialog
import com.gudangku.app.ui.components.EmptyState
import com.gudangku.app.ui.components.LoadingState
import com.gudangku.app.ui.components.LowStockBadge
import com.gudangku.app.ui.components.formatCurrency
import com.gudangku.app.ui.components.formatDateTime
import com.gudangku.app.viewmodel.ProductViewModel
import com.gudangku.app.viewmodel.TransactionViewModel
import com.gudangku.app.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Long,
    factory: ViewModelFactory,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onStockInClick: () -> Unit,
    onStockOutClick: () -> Unit,
    onDeleted: () -> Unit
) {
    val productViewModel: ProductViewModel = viewModel(factory = factory)
    val transactionViewModel: TransactionViewModel = viewModel(factory = factory)

    val product by productViewModel.getProductById(productId).collectAsState(initial = null)
    val history by transactionViewModel.getTransactionsForProduct(productId).collectAsState(initial = emptyList())

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.name ?: "Detail Barang") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Barang")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Hapus Barang")
                    }
                }
            )
        }
    ) { padding ->
        val currentProduct = product
        if (currentProduct == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = currentProduct.name, style = MaterialTheme.typography.titleLarge)
                        Text(text = "SKU: ${currentProduct.sku}", color = MaterialTheme.colorScheme.outline)
                        if (currentProduct.isLowStock) {
                            LowStockBadge()
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow("Kategori", currentProduct.category)
                        DetailRow("Stok Saat Ini", "${currentProduct.currentStock} ${currentProduct.unit}")
                        DetailRow("Stok Minimum", "${currentProduct.minStock} ${currentProduct.unit}")
                        DetailRow("Harga Beli", formatCurrency(currentProduct.purchasePrice))
                        DetailRow("Lokasi Gudang", currentProduct.location)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onStockInClick, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.ArrowDownward, contentDescription = null)
                        Text(" Stok Masuk")
                    }
                    OutlinedButton(onClick = onStockOutClick, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.ArrowUpward, contentDescription = null)
                        Text(" Stok Keluar")
                    }
                }
            }

            item {
                Text(
                    text = "Riwayat Pergerakan Stok",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (history.isEmpty()) {
                item { EmptyState(message = "Belum ada riwayat transaksi untuk barang ini.") }
            } else {
                items(history, key = { it.transactionId }) { tx ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (tx.transactionType == TransactionType.STOK_MASUK) "Stok Masuk" else "Stok Keluar",
                                    fontWeight = FontWeight.Bold,
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

        if (showDeleteDialog) {
            ConfirmDialog(
                title = "Hapus Barang",
                message = "Apakah Anda yakin ingin menghapus \"${currentProduct.name}\"? Tindakan ini tidak dapat dibatalkan.",
                onConfirm = {
                    showDeleteDialog = false
                    productViewModel.deleteProduct(currentProduct) { result ->
                        if (result is OperationResult.Success) {
                            onDeleted()
                        }
                    }
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.outline)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}
