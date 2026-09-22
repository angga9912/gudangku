package com.gudangku.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gudangku.app.data.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE productId = :productId LIMIT 1")
    fun getProductById(productId: Long): Flow<Product?>

    @Query("SELECT * FROM products WHERE productId = :productId LIMIT 1")
    suspend fun getProductByIdOnce(productId: Long): Product?

    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): Product?

    @Query(
        "SELECT * FROM products WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%') " +
            "AND (:category = '' OR category = :category) " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'name' THEN name END ASC, " +
            "CASE WHEN :sortBy = 'stock' THEN currentStock END ASC, " +
            "CASE WHEN :sortBy = 'date' THEN updatedAt END DESC"
    )
    fun searchProducts(query: String, category: String, sortBy: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE currentStock <= minStock ORDER BY currentStock ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("SELECT COUNT(*) FROM products")
    fun getTotalProductCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(currentStock), 0) FROM products")
    fun getTotalStockCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("UPDATE products SET currentStock = currentStock + :amount, updatedAt = :now WHERE productId = :productId")
    suspend fun addStock(productId: Long, amount: Int, now: Long = System.currentTimeMillis())

    @Query("UPDATE products SET currentStock = currentStock - :amount, updatedAt = :now WHERE productId = :productId")
    suspend fun subtractStock(productId: Long, amount: Int, now: Long = System.currentTimeMillis())
}
