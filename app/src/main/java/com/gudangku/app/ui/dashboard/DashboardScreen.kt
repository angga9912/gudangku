package com.gudangku.app.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.gudangku.app.ui.components.LoadingState
import com.gudangku.app.ui.components.LowStockBadge
import com.gudangku.app.ui.components.StatCard
import com.gudangku.app.viewmodel.DashboardViewModel
import com.gudangku.app.viewmodel.ViewModelFactory

@Composable
fun DashboardScreen(
    factory: ViewModelFactory,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToStockIn: () -> Unit,
    onNavigateToStockOut: () -> Unit,
    onNavigateToProductDetail: (Long) -> Unit
) {
    val viewModel: DashboardViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("GudangKu") }) }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        title = "Jenis Barang",
                        value = "${uiState.totalProductTypes}",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Stok",
                        value = "${uiState.totalStock}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        title = "Stok Masuk Hari Ini",
                        value = "${uiState.stockInToday}",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Stok Keluar Hari Ini",
                        value = "${uiState.stockOutToday}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(onClick = onNavigateToAddProduct, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Inventory, contentDescription = null, modifier = Modifier.height(18.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("Tambah Barang")
                    }
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(onClick = onNavigateToStockIn, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.ArrowDownward, contentDescription = null)
                        Text(" Stok Masuk")
                    }
                    OutlinedButton(onClick = onNavigateToStockOut, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.ArrowUpward, contentDescription = null)
                        Text(" Stok Keluar")
                    }
                }
            }

            item {
                Text(
                    text = "Barang Stok Menipis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (uiState.lowStockProducts.isEmpty()) {
                item {
                    Text(
                        text = "Semua stok barang aman.",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                items(uiState.lowStockProducts, key = { it.productId }) { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToProductDetail(product.productId) }
                            .padding(12.dp)
                        ) {
                            Text(text = product.name, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Stok: ${product.currentStock} ${product.unit} (min ${product.minStock})",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            LowStockBadge()
                        }
                    }
                }
            }
        }
    }
}
