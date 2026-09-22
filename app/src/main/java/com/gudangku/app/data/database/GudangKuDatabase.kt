package com.gudangku.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gudangku.app.data.dao.CategoryDao
import com.gudangku.app.data.dao.ProductDao
import com.gudangku.app.data.dao.StockTransactionDao
import com.gudangku.app.data.entity.Category
import com.gudangku.app.data.entity.Product
import com.gudangku.app.data.entity.StockTransaction
import com.gudangku.app.data.entity.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [Product::class, StockTransaction::class, Category::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GudangKuDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun stockTransactionDao(): StockTransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: GudangKuDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): GudangKuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GudangKuDatabase::class.java,
                    "gudangku_database"
                )
                    .addCallback(SeedDataCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback ini mengisi data demo (kategori, barang, dan beberapa transaksi
     * contoh) saat database dibuat pertama kali.
     */
    private class SeedDataCallback(private val scope: CoroutineScope) : Callback() {
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    seedDatabase(database)
                }
            }
        }

        private suspend fun seedDatabase(database: GudangKuDatabase) {
            val categoryDao = database.categoryDao()
            val productDao = database.productDao()
            val transactionDao = database.stockTransactionDao()

            categoryDao.insertAll(
                listOf(
                    Category(name = "Minuman"),
                    Category(name = "Makanan"),
                    Category(name = "Sembako")
                )
            )

            val now = System.currentTimeMillis()
            val demoProducts = listOf(
                Product(
                    sku = "MNM-001",
                    name = "Kopi Sachet",
                    category = "Minuman",
                    unit = "pcs",
                    currentStock = 120,
                    minStock = 30,
                    purchasePrice = 1500.0,
                    location = "Rak A1",
                    createdAt = now,
                    updatedAt = now
                ),
                Product(
                    sku = "MNM-002",
                    name = "Teh Celup",
                    category = "Minuman",
                    unit = "box",
                    currentStock = 25,
                    minStock = 10,
                    purchasePrice = 8500.0,
                    location = "Rak A2",
                    createdAt = now,
                    updatedAt = now
                ),
                Product(
                    sku = "SMB-001",
                    name = "Gula 1 Kg",
                    category = "Sembako",
                    unit = "kg",
                    currentStock = 8,
                    minStock = 10,
                    purchasePrice = 14000.0,
                    location = "Rak B1",
                    createdAt = now,
                    updatedAt = now
                ),
                Product(
                    sku = "MNM-003",
                    name = "Air Mineral",
                    category = "Minuman",
                    unit = "box",
                    currentStock = 40,
                    minStock = 15,
                    purchasePrice = 32000.0,
                    location = "Rak A3",
                    createdAt = now,
                    updatedAt = now
                ),
                Product(
                    sku = "MKN-001",
                    name = "Mie Instan",
                    category = "Makanan",
                    unit = "pcs",
                    currentStock = 5,
                    minStock = 20,
                    purchasePrice = 3000.0,
                    location = "Rak C1",
                    createdAt = now,
                    updatedAt = now
                )
            )

            val productIds = demoProducts.map { productDao.insert(it) }

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -2)
            val twoDaysAgo = calendar.timeInMillis

            val demoTransactions = listOf(
                StockTransaction(
                    productId = productIds[0],
                    productName = demoProducts[0].name,
                    transactionType = TransactionType.STOK_MASUK,
                    quantity = 100,
                    date = twoDaysAgo,
                    note = "Stok awal",
                    referenceNumber = "PO-0001",
                    supplierOrDestination = "Supplier Kopi Jaya"
                ),
                StockTransaction(
                    productId = productIds[0],
                    productName = demoProducts[0].name,
                    transactionType = TransactionType.STOK_KELUAR,
                    quantity = 20,
                    date = now,
                    note = "Penjualan harian",
                    referenceNumber = null,
                    supplierOrDestination = "Toko"
                ),
                StockTransaction(
                    productId = productIds[2],
                    productName = demoProducts[2].name,
                    transactionType = TransactionType.STOK_MASUK,
                    quantity = 18,
                    date = twoDaysAgo,
                    note = "Stok awal",
                    referenceNumber = "PO-0002",
                    supplierOrDestination = "Distributor Sembako"
                ),
                StockTransaction(
                    productId = productIds[2],
                    productName = demoProducts[2].name,
                    transactionType = TransactionType.STOK_KELUAR,
                    quantity = 10,
                    date = now,
                    note = "Penjualan harian",
                    referenceNumber = null,
                    supplierOrDestination = "Toko"
                ),
                StockTransaction(
                    productId = productIds[4],
                    productName = demoProducts[4].name,
                    transactionType = TransactionType.STOK_MASUK,
                    quantity = 25,
                    date = twoDaysAgo,
                    note = "Stok awal",
                    referenceNumber = "PO-0003",
                    supplierOrDestination = "Distributor Makanan"
                ),
                StockTransaction(
                    productId = productIds[4],
                    productName = demoProducts[4].name,
                    transactionType = TransactionType.STOK_KELUAR,
                    quantity = 20,
                    date = now,
                    note = "Penjualan harian",
                    referenceNumber = null,
                    supplierOrDestination = "Toko"
                )
            )

            transactionDao.insertAll(demoTransactions)
        }
    }
}
