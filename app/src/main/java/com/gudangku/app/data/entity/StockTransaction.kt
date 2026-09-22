package com.gudangku.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity untuk transaksi pergerakan stok (masuk/keluar).
 * Setiap transaksi terhubung ke satu Product (relasi 1 -> banyak).
 */
@Entity(
    tableName = "stock_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["productId"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId")]
)
data class StockTransaction(
    @PrimaryKey(autoGenerate = true)
    val transactionId: Long = 0,
    val productId: Long,
    val productName: String,
    val transactionType: TransactionType,
    val quantity: Int,
    val date: Long,
    val note: String? = null,
    val referenceNumber: String? = null,
    val supplierOrDestination: String? = null
)
