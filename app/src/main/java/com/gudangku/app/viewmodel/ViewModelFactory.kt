package com.gudangku.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.gudangku.app.GudangKuApp

/**
 * Factory sederhana untuk membuat ViewModel dengan dependency dari GudangKuApp.
 */
class ViewModelFactory(private val app: GudangKuApp) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(app.productRepository, app.transactionRepository) as T

            modelClass.isAssignableFrom(ProductViewModel::class.java) ->
                ProductViewModel(app.productRepository, app.categoryRepository) as T

            modelClass.isAssignableFrom(TransactionViewModel::class.java) ->
                TransactionViewModel(app.transactionRepository, app.productRepository) as T

            modelClass.isAssignableFrom(ReportViewModel::class.java) ->
                ReportViewModel(app.productRepository, app.transactionRepository) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
