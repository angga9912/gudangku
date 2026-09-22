package com.gudangku.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity untuk data barang di gudang.
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val productId: Long = 0,
    val sku: String,
    val name: String,
    val category: String,
    val unit: String,
    val currentStock: Int,
    val minStock: Int,
    val purchasePrice: Double,
    val location: String,
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = currentStock <= minStock
}
