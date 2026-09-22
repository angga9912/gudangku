package com.gudangku.app.ui.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
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
import com.gudangku.app.viewmodel.ProductFormEvent
import com.gudangku.app.viewmodel.ProductViewModel
import com.gudangku.app.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: Long?,
    factory: ViewModelFactory,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val viewModel: ProductViewModel = viewModel(factory = factory)
    val isEditing = productId != null
    val existingProduct by if (productId != null) {
        viewModel.getProductById(productId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf<Product?>(null) }
    }

    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("0") }
    var minStock by remember { mutableStateOf("0") }
    var price by remember { mutableStateOf("0") }
    var location by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(existingProduct) {
        val p = existingProduct
        if (p != null && !initialized) {
            name = p.name
            sku = p.sku
            category = p.category
            unit = p.unit
            stock = p.currentStock.toString()
            minStock = p.minStock.toString()
            price = p.purchasePrice.toString()
            location = p.location
            initialized = true
        }
    }

    val formEvent by viewModel.formEvent.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(formEvent) {
        when (val event = formEvent) {
            is ProductFormEvent.Success -> {
                viewModel.resetFormEvent()
                onSaved()
            }
            is ProductFormEvent.Error -> {
                scope.launch { snackbarHostState.showSnackbar(event.message) }
                viewModel.resetFormEvent()
            }
            else -> Unit
        }
    }

    var nameError by remember { mutableStateOf(false) }
    var skuError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Barang" else "Tambah Barang") },
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
            FormTextField(
                label = "Nama Barang*",
                value = name,
                onValueChange = { name = it; nameError = false },
                isError = nameError,
                supportingText = if (nameError) "Nama barang wajib diisi" else null
            )
            FormTextField(
                label = "SKU / Kode Barang*",
                value = sku,
                onValueChange = { sku = it; skuError = false },
                isError = skuError,
                supportingText = if (skuError) "SKU wajib diisi" else null
            )
            FormTextField(label = "Kategori", value = category, onValueChange = { category = it })
            FormTextField(label = "Satuan (pcs, box, kg, liter, dll)", value = unit, onValueChange = { unit = it })
            FormTextField(
                label = "Stok Saat Ini",
                value = stock,
                onValueChange = { if (it.all(Char::isDigit) || it.isEmpty()) stock = it },
                keyboardType = KeyboardType.Number
            )
            FormTextField(
                label = "Stok Minimum",
                value = minStock,
                onValueChange = { if (it.all(Char::isDigit) || it.isEmpty()) minStock = it },
                keyboardType = KeyboardType.Number
            )
            FormTextField(
                label = "Harga Beli",
                value = price,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$"))) price = it },
                keyboardType = KeyboardType.Number
            )
            FormTextField(label = "Lokasi / Rak Gudang", value = location, onValueChange = { location = it })

            Button(
                onClick = {
                    nameError = name.isBlank()
                    skuError = sku.isBlank()
                    if (nameError || skuError) return@Button

                    val product = Product(
                        productId = productId ?: 0,
                        sku = sku,
                        name = name,
                        category = category.ifBlank { "Lainnya" },
                        unit = unit.ifBlank { "pcs" },
                        currentStock = stock.toIntOrNull() ?: 0,
                        minStock = minStock.toIntOrNull() ?: 0,
                        purchasePrice = price.toDoubleOrNull() ?: 0.0,
                        location = location,
                        createdAt = existingProduct?.createdAt ?: System.currentTimeMillis()
                    )
                    viewModel.saveProduct(product, isEditing)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Simpan Perubahan" else "Simpan Barang")
            }
        }
    }
}
