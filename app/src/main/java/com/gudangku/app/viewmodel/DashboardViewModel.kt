package com.gudangku.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gudangku.app.data.entity.Product
import com.gudangku.app.data.entity.TransactionType
import com.gudangku.app.data.repository.ProductRepository
import com.gudangku.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val totalProductTypes: Int = 0,
    val totalStock: Int = 0,
    val lowStockProducts: List<Product> = emptyList(),
    val stockInToday: Int = 0,
    val stockOutToday: Int = 0,
    val isLoading: Boolean = true
)

class DashboardViewModel(
    productRepository: ProductRepository,
    transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        productRepository.totalProductCount,
        productRepository.totalStockCount,
        productRepository.lowStockProducts,
        transactionRepository.getTodayTransactionCount(TransactionType.STOK_MASUK),
        transactionRepository.getTodayTransactionCount(TransactionType.STOK_KELUAR)
    ) { totalTypes, totalStock, lowStock, inToday, outToday ->
        DashboardUiState(
            totalProductTypes = totalTypes,
            totalStock = totalStock,
            lowStockProducts = lowStock,
            stockInToday = inToday,
            stockOutToday = outToday,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}
