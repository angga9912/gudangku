package com.gudangku.app.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gudangku.app.data.entity.Product
import com.gudangku.app.ui.components.FormTextField
import com.gudangku.app.viewmodel.ProductViewModel
import com.gudangku.app.viewmodel.TransactionFormEvent
import com.gudangku.app.viewmodel.TransactionViewModel
import com.gudangku.app.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockInScreen(
    preselectedProductId: Long?,
    factory: ViewModelFactory,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val productViewModel: ProductViewModel = viewModel(factory = factory)
    val transactionViewModel: TransactionViewModel = viewModel(factory = factory)
    val productListState by productViewModel.uiState.collectAsState()

    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var productQuery by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var referenceNumber by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var quantityError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(preselectedProductId, productListState.products) {
        if (preselectedProductId != null && selectedProduct == null) {
            selectedProduct = productListState.products.find { it.productId == preselectedProductId }
        }
    }

    val formEvent by transactionViewModel.formEvent.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(formEvent) {
        when (val event = formEvent) {
            is TransactionFormEvent.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Stok berhasil ditambahkan") }
                transactionViewModel.resetFormEvent()
                onSaved()
            }
            is TransactionFormEvent.Error -> {
                scope.launch { snackbarHostState.showSnackbar(event.message) }
                transactionViewModel.resetFormEvent()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stok Masuk") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val current = selectedProduct
            if (current == null) {
                FormTextField(
                    label = "Cari barang...",
                    value = productQuery,
                    onValueChange = {
                        productQuery = it
                        productViewModel.onSearchQueryChange(it)
                    }
                )
                LazyColumn(
                    modifier = Modifier.height(220.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(productListState.products, key = { it.productId }) { p ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .then(Modifier)
                                    .clickableSelect { selectedProduct = p }
                            ) {
                                Text(text = p.name)
                                Text(
                                    text = "${p.sku} · Stok: ${p.currentStock} ${p.unit}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = current.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Stok saat ini: ${current.currentStock} ${current.unit}")
                        Button(onClick = { selectedProduct = null }) {
                            Text("Ganti Barang")
                        }
                    }
                }

                FormTextField(
                    label = "Jumlah*",
                    value = quantity,
                    onValueChange = { if (it.all(Char::isDigit) || it.isEmpty()) { quantity = it; quantityError = null } },
                    keyboardType = KeyboardType.Number,
                    isError = quantityError != null,
                    supportingText = quantityError
                )
                FormTextField(label = "Supplier (opsional)", value = supplier, onValueChange = { supplier = it })
                FormTextField(label = "Nomor Referensi (opsional)", value = referenceNumber, onValueChange = { referenceNumber = it })
                FormTextField(label = "Catatan", value = note, onValueChange = { note = it })

                Button(
                    onClick = {
                        val qty = quantity.toIntOrNull() ?: 0
                        if (qty <= 0) {
                            quantityError = "Jumlah stok harus lebih besar dari 0"
                            return@Button
                        }
                        transactionViewModel.recordStockIn(
                            product = current,
                            quantity = qty,
                            date = System.currentTimeMillis(),
                            supplier = supplier.ifBlank { null },
                            referenceNumber = referenceNumber.ifBlank { null },
                            note = note.ifBlank { null }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simpan Stok Masuk")
                }
            }
        }
    }
}

private fun Modifier.clickableSelect(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
