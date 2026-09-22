package com.gudangku.app.data.repository

import com.gudangku.app.data.dao.ProductDao
import com.gudangku.app.data.dao.StockTransactionDao
import com.gudangku.app.data.entity.StockTransaction
import com.gudangku.app.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TransactionRepository(
    private val transactionDao: StockTransactionDao,
    private val productDao: ProductDao
) {

    val allTransactions: Flow<List<StockTransaction>> = transactionDao.getAllTransactions()

    fun getTransactionsForProduct(productId: Long): Flow<List<StockTransaction>> =
        transactionDao.getTransactionsForProduct(productId)

    fun searchTransactions(
        query: String,
        type: TransactionType?,
        startDate: Long,
        endDate: Long
    ): Flow<List<StockTransaction>> =
        transactionDao.searchTransactions(query.trim(), type?.name ?: "", startDate, endDate)

    fun getTodayTransactionCount(type: TransactionType): Flow<Int> {
        val (start, end) = todayRange()
        return transactionDao.getTodayTransactionCount(type, start, end)
    }

    fun getTotalQuantityByType(type: TransactionType, startDate: Long, endDate: Long): Flow<Int> =
        transactionDao.getTotalQuantityByType(type, startDate, endDate)

    /**
     * Mencatat transaksi stok masuk dan menambah stok barang terkait.
     * Jumlah harus lebih besar dari 0.
     */
    suspend fun recordStockIn(
        productId: Long,
        productName: String,
        quantity: Int,
        date: Long,
        supplier: String?,
        referenceNumber: String?,
        note: String?
    ): OperationResult {
        if (quantity <= 0) {
            return OperationResult.Error("Jumlah stok harus lebih besar dari 0")
        }
        return try {
            transactionDao.insert(
                StockTransaction(
                    productId = productId,
                    productName = productName,
                    transactionType = TransactionType.STOK_MASUK,
                    quantity = quantity,
                    date = date,
                    note = note,
                    referenceNumber = referenceNumber,
                    supplierOrDestination = supplier
                )
            )
            productDao.addStock(productId, quantity)
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error("Gagal menyimpan transaksi: ${e.localizedMessage ?: "kesalahan tidak diketahui"}")
        }
    }

    /**
     * Mencatat transaksi stok keluar dan mengurangi stok barang terkait.
     * Tidak diizinkan jika jumlah melebihi stok yang tersedia (stok tidak boleh negatif).
     */
    suspend fun recordStockOut(
        productId: Long,
        productName: String,
        currentStock: Int,
        quantity: Int,
        date: Long,
        destination: String?,
        referenceNumber: String?,
        note: String?
    ): OperationResult {
        if (quantity <= 0) {
            return OperationResult.Error("Jumlah stok harus lebih besar dari 0")
        }
        if (quantity > currentStock) {
            return OperationResult.Error("Stok keluar melebihi stok tersedia ($currentStock)")
        }
        return try {
            transactionDao.insert(
                StockTransaction(
                    productId = productId,
                    productName = productName,
                    transactionType = TransactionType.STOK_KELUAR,
                    quantity = quantity,
                    date = date,
                    note = note,
                    referenceNumber = referenceNumber,
                    supplierOrDestination = destination
                )
            )
            productDao.subtractStock(productId, quantity)
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error("Gagal menyimpan transaksi: ${e.localizedMessage ?: "kesalahan tidak diketahui"}")
        }
    }

    private fun todayRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        return start to end
    }
}
