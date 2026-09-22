package com.gudangku.app.data.repository

import com.gudangku.app.data.dao.ProductDao
import com.gudangku.app.data.entity.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
    val totalProductCount: Flow<Int> = productDao.getTotalProductCount()
    val totalStockCount: Flow<Int> = productDao.getTotalStockCount()

    fun getProductById(productId: Long): Flow<Product?> = productDao.getProductById(productId)

    fun searchProducts(query: String, category: String, sortBy: String): Flow<List<Product>> =
        productDao.searchProducts(query.trim(), category, sortBy)

    /**
     * Menambahkan barang baru dengan validasi:
     * - Nama wajib diisi
     * - SKU wajib diisi dan tidak boleh duplikat
     * - Harga tidak boleh negatif
     */
    suspend fun addProduct(product: Product): OperationResult {
        if (product.name.isBlank()) {
            return OperationResult.Error("Nama barang wajib diisi")
        }
        if (product.sku.isBlank()) {
            return OperationResult.Error("SKU wajib diisi")
        }
        if (product.purchasePrice < 0) {
            return OperationResult.Error("Harga tidak boleh negatif")
        }
        val existing = productDao.getProductBySku(product.sku.trim())
        if (existing != null) {
            return OperationResult.Error("SKU sudah digunakan oleh barang lain")
        }
        return try {
            productDao.insert(product.copy(sku = product.sku.trim(), name = product.name.trim()))
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error("Gagal menyimpan barang: ${e.localizedMessage ?: "kesalahan tidak diketahui"}")
        }
    }

    suspend fun updateProduct(product: Product): OperationResult {
        if (product.name.isBlank()) {
            return OperationResult.Error("Nama barang wajib diisi")
        }
        if (product.sku.isBlank()) {
            return OperationResult.Error("SKU wajib diisi")
        }
        if (product.purchasePrice < 0) {
            return OperationResult.Error("Harga tidak boleh negatif")
        }
        val existing = productDao.getProductBySku(product.sku.trim())
        if (existing != null && existing.productId != product.productId) {
            return OperationResult.Error("SKU sudah digunakan oleh barang lain")
        }
        return try {
            productDao.update(
                product.copy(
                    sku = product.sku.trim(),
                    name = product.name.trim(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error("Gagal memperbarui barang: ${e.localizedMessage ?: "kesalahan tidak diketahui"}")
        }
    }

    suspend fun deleteProduct(product: Product): OperationResult {
        return try {
            productDao.delete(product)
            OperationResult.Success
        } catch (e: Exception) {
            OperationResult.Error("Gagal menghapus barang: ${e.localizedMessage ?: "kesalahan tidak diketahui"}")
        }
    }
}
