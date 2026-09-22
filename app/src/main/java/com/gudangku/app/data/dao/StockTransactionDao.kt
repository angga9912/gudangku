package com.gudangku.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gudangku.app.data.entity.StockTransaction
import com.gudangku.app.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface StockTransactionDao {

    @Query("SELECT * FROM stock_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE productId = :productId ORDER BY date DESC")
    fun getTransactionsForProduct(productId: Long): Flow<List<StockTransaction>>

    @Query(
        "SELECT * FROM stock_transactions WHERE " +
            "(:query = '' OR productName LIKE '%' || :query || '%') " +
            "AND (:type = '' OR transactionType = :type) " +
            "AND date >= :startDate AND date <= :endDate " +
            "ORDER BY date DESC"
    )
    fun searchTransactions(
        query: String,
        type: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<StockTransaction>>

    @Query(
        "SELECT COUNT(*) FROM stock_transactions WHERE transactionType = :type " +
            "AND date >= :startOfDay AND date <= :endOfDay"
    )
    fun getTodayTransactionCount(type: TransactionType, startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query(
        "SELECT COALESCE(SUM(quantity), 0) FROM stock_transactions WHERE transactionType = :type " +
            "AND date >= :startDate AND date <= :endDate"
    )
    fun getTotalQuantityByType(type: TransactionType, startDate: Long, endDate: Long): Flow<Int>

    @Insert
    suspend fun insert(transaction: StockTransaction): Long

    @Insert
    suspend fun insertAll(transactions: List<StockTransaction>)
}
