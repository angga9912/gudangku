package com.gudangku.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gudangku.app.data.entity.Product
import com.gudangku.app.data.entity.StockTransaction
import com.gudangku.app.data.entity.TransactionType
import com.gudangku.app.data.repository.OperationResult
import com.gudangku.app.data.repository.ProductRepository
import com.gudangku.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryFilter(val type: TransactionType?, val label: String) {
    ALL(null, "Semua"),
    IN(TransactionType.STOK_MASUK, "Stok Masuk"),
    OUT(TransactionType.STOK_KELUAR, "Stok Keluar")
}

sealed class TransactionFormEvent {
    object Idle : TransactionFormEvent()
    object Saving : TransactionFormEvent()
    object Success : TransactionFormEvent()
    data class Error(val message: String) : TransactionFormEvent()
}

data class TransactionHistoryUiState(
    val transactions: List<StockTransaction> = emptyList(),
    val searchQuery: String = "",
    val filter: HistoryFilter = HistoryFilter.ALL,
    val isLoading: Boolean = true
)

class TransactionViewModel(
    private val transactionRepository: TransactionRepository,
    val productRepository: ProductRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val filter = MutableStateFlow(HistoryFilter.ALL)

    private val filteredTransactions = combine(searchQuery, filter) { q, f -> q to f }
        .flatMapLatest { (q, f) ->
            transactionRepository.searchTransactions(q, f.type, 0L, Long.MAX_VALUE)
        }

    val uiState: StateFlow<TransactionHistoryUiState> = combine(
        filteredTransactions, searchQuery, filter
    ) { transactions, q, f ->
        TransactionHistoryUiState(transactions, q, f, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionHistoryUiState()
    )

    private val _formEvent = MutableStateFlow<TransactionFormEvent>(TransactionFormEvent.Idle)
    val formEvent: StateFlow<TransactionFormEvent> = _formEvent

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onFilterChange(newFilter: HistoryFilter) {
        filter.value = newFilter
    }

    fun getTransactionsForProduct(productId: Long) =
        transactionRepository.getTransactionsForProduct(productId)

    fun recordStockIn(
        product: Product,
        quantity: Int,
        date: Long,
        supplier: String?,
        referenceNumber: String?,
        note: String?
    ) {
        viewModelScope.launch {
            _formEvent.value = TransactionFormEvent.Saving
            val result = transactionRepository.recordStockIn(
                productId = product.productId,
                productName = product.name,
                quantity = quantity,
                date = date,
                supplier = supplier,
                referenceNumber = referenceNumber,
                note = note
            )
            _formEvent.value = when (result) {
                is OperationResult.Success -> TransactionFormEvent.Success
                is OperationResult.Error -> TransactionFormEvent.Error(result.message)
            }
        }
    }

    fun recordStockOut(
        product: Product,
        quantity: Int,
        date: Long,
        destination: String?,
        referenceNumber: String?,
        note: String?
    ) {
        viewModelScope.launch {
            _formEvent.value = TransactionFormEvent.Saving
            val result = transactionRepository.recordStockOut(
                productId = product.productId,
                productName = product.name,
                currentStock = product.currentStock,
                quantity = quantity,
                date = date,
                destination = destination,
                referenceNumber = referenceNumber,
                note = note
            )
            _formEvent.value = when (result) {
                is OperationResult.Success -> TransactionFormEvent.Success
                is OperationResult.Error -> TransactionFormEvent.Error(result.message)
            }
        }
    }

    fun resetFormEvent() {
        _formEvent.value = TransactionFormEvent.Idle
    }
}
