package com.gudangku.app.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object ProductList : Screen("product_list")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Long) = "product_detail/$productId"
    }
    object AddEditProduct : Screen("add_edit_product?productId={productId}") {
        fun createRoute(productId: Long? = null) =
            if (productId != null) "add_edit_product?productId=$productId" else "add_edit_product"
    }
    object StockIn : Screen("stock_in?productId={productId}") {
        fun createRoute(productId: Long? = null) =
            if (productId != null) "stock_in?productId=$productId" else "stock_in"
    }
    object StockOut : Screen("stock_out?productId={productId}") {
        fun createRoute(productId: Long? = null) =
            if (productId != null) "stock_out?productId=$productId" else "stock_out"
    }
    object TransactionHistory : Screen("transaction_history")
    object Report : Screen("report")
    object Settings : Screen("settings")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
