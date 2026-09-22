package com.gudangku.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gudangku.app.data.entity.Category
import com.gudangku.app.data.entity.Product
import com.gudangku.app.data.repository.CategoryRepository
import com.gudangku.app.data.repository.OperationResult
import com.gudangku.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption(val dbValue: String, val label: String) {
    NAME("name", "Nama"),
    STOCK("stock", "Stok"),
    DATE("date", "Tanggal")
}

data class ProductListUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "",
    val sortOption: SortOption = SortOption.NAME,
    val isLoading: Boolean = true
)

sealed class ProductFormEvent {
    object Idle : ProductFormEvent()
    object Saving : ProductFormEvent()
    object Success : ProductFormEvent()
    data class Error(val message: String) : ProductFormEvent()
}

class ProductViewModel(
    private val productRepository: ProductRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow("")
    private val sortOption = MutableStateFlow(SortOption.NAME)

    private val filteredProducts = combine(searchQuery, selectedCategory, sortOption) { q, c, s ->
        Triple(q, c, s)
    }.flatMapLatest { (q, c, s) ->
        productRepository.searchProducts(q, c, s.dbValue)
    }

    val uiState: StateFlow<ProductListUiState> = combine(
        filteredProducts,
        categoryRepository.allCategories,
        searchQuery,
        selectedCategory,
        sortOption
    ) { products, categories, q, c, s ->
        ProductListUiState(
            products = products,
            categories = categories,
            searchQuery = q,
            selectedCategory = c,
            sortOption = s,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProductListUiState()
    )

    private val _formEvent = MutableStateFlow<ProductFormEvent>(ProductFormEvent.Idle)
    val formEvent: StateFlow<ProductFormEvent> = _formEvent

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onCategoryFilterChange(category: String) {
        selectedCategory.value = category
    }

    fun onSortOptionChange(option: SortOption) {
        sortOption.value = option
    }

    fun getProductById(productId: Long) = productRepository.getProductById(productId)

    fun saveProduct(product: Product, isEditing: Boolean) {
        viewModelScope.launch {
            _formEvent.value = ProductFormEvent.Saving
            val result = if (isEditing) {
                productRepository.updateProduct(product)
            } else {
                productRepository.addProduct(product)
            }
            _formEvent.value = when (result) {
                is OperationResult.Success -> ProductFormEvent.Success
                is OperationResult.Error -> ProductFormEvent.Error(result.message)
            }
        }
    }

    fun deleteProduct(product: Product, onResult: (OperationResult) -> Unit) {
        viewModelScope.launch {
            val result = productRepository.deleteProduct(product)
            onResult(result)
        }
    }

    fun resetFormEvent() {
        _formEvent.value = ProductFormEvent.Idle
    }
}
