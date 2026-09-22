package com.gudangku.app

import android.app.Application
import com.gudangku.app.data.database.GudangKuDatabase
import com.gudangku.app.data.repository.CategoryRepository
import com.gudangku.app.data.repository.ProductRepository
import com.gudangku.app.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Application class yang menyediakan instance database dan repository
 * secara singleton untuk seluruh aplikasi (dipakai oleh ViewModelFactory).
 */
class GudangKuApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    val database: GudangKuDatabase by lazy {
        GudangKuDatabase.getDatabase(this, applicationScope)
    }

    val productRepository: ProductRepository by lazy {
        ProductRepository(database.productDao())
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepository(database.stockTransactionDao(), database.productDao())
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(database.categoryDao())
    }
}
