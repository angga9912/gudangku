package com.gudangku.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gudangku.app.data.entity.Product
import com.gudangku.app.data.entity.StockTransaction
import com.gudangku.app.data.entity.TransactionType
import com.gudangku.app.data.repository.ProductRepository
import com.gudangku.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class ReportPeriod(val label: String) {
    TODAY("Hari Ini"),
    LAST_7_DAYS("7 Hari Terakhir"),
    THIS_MONTH("Bulan Ini"),
    CUSTOM("Custom Tanggal")
}

data class ReportUiState(
    val period: ReportPeriod = ReportPeriod.LAST_7_DAYS,
    val customStart: Long = 0L,
    val customEnd: Long = System.currentTimeMillis(),
    val totalStock: Int = 0,
    val totalStockIn: Int = 0,
    val totalStockOut: Int = 0,
    val lowStockProducts: List<Product> = emptyList(),
    val transactions: List<StockTransaction> = emptyList(),
    val isLoading: Boolean = true
)

class ReportViewModel(
    productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val period = MutableStateFlow(ReportPeriod.LAST_7_DAYS)
    private val customRange = MutableStateFlow(getRangeForPeriod(ReportPeriod.LAST_7_DAYS))

    private val range = combine(period, customRange) { p, custom ->
        if (p == ReportPeriod.CUSTOM) custom else getRangeForPeriod(p)
    }

    private val stockInTotal = range.flatMapLatest { (start, end) ->
        transactionRepository.getTotalQuantityByType(TransactionType.STOK_MASUK, start, end)
    }
    private val stockOutTotal = range.flatMapLatest { (start, end) ->
        transactionRepository.getTotalQuantityByType(TransactionType.STOK_KELUAR, start, end)
    }
    private val transactionsInRange = range.flatMapLatest { (start, end) ->
        transactionRepository.searchTransactions("", null, start, end)
    }

    val uiState: StateFlow<ReportUiState> = combine(
        period,
        productRepository.totalStockCount,
        stockInTotal,
        stockOutTotal,
        productRepository.lowStockProducts
    ) { p, totalStock, stockIn, stockOut, lowStock ->
        ReportUiState(
            period = p,
            totalStock = totalStock,
            totalStockIn = stockIn,
            totalStockOut = stockOut,
            lowStockProducts = lowStock,
            isLoading = false
        )
    }.combine(transactionsInRange) { state, transactions ->
        state.copy(transactions = transactions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportUiState()
    )

    fun onPeriodChange(newPeriod: ReportPeriod) {
        period.value = newPeriod
    }

    fun onCustomRangeChange(start: Long, end: Long) {
        customRange.value = start to end
        period.value = ReportPeriod.CUSTOM
    }

    private fun getRangeForPeriod(p: ReportPeriod): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val end = cal.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val startCal = Calendar.getInstance()
        when (p) {
            ReportPeriod.TODAY -> {
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)
            }
            ReportPeriod.LAST_7_DAYS -> {
                startCal.add(Calendar.DAY_OF_YEAR, -6)
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)
            }
            ReportPeriod.THIS_MONTH -> {
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)
            }
            ReportPeriod.CUSTOM -> {
                // ditangani di luar fungsi ini via customRange
            }
        }
        return startCal.timeInMillis to end
    }
}
