package com.gudangku.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.gudangku.app.GudangKuApp
import com.gudangku.app.ui.dashboard.DashboardScreen
import com.gudangku.app.ui.product.AddEditProductScreen
import com.gudangku.app.ui.product.ProductDetailScreen
import com.gudangku.app.ui.product.ProductListScreen
import com.gudangku.app.ui.report.ReportScreen
import com.gudangku.app.ui.settings.SettingsScreen
import com.gudangku.app.ui.transaction.StockInScreen
import com.gudangku.app.ui.transaction.StockOutScreen
import com.gudangku.app.ui.transaction.TransactionHistoryScreen
import com.gudangku.app.viewmodel.ViewModelFactory

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Dashboard", Icons.Outlined.Dashboard),
    BottomNavItem(Screen.ProductList, "Barang", Icons.Filled.Inventory2),
    BottomNavItem(Screen.TransactionHistory, "Transaksi", Icons.Filled.SwapHoriz),
    BottomNavItem(Screen.Settings, "Pengaturan", Icons.Filled.Settings)
)

@Composable
fun GudangKuNavGraph(app: GudangKuApp) {
    val navController = rememberNavController()
    val factory = ViewModelFactory(app)

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination
            val showBottomBar = bottomNavItems.any { item ->
                currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
            }
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.screen.route
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { androidx.compose.material3.Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    factory = factory,
                    onNavigateToAddProduct = { navController.navigate(Screen.AddEditProduct.createRoute()) },
                    onNavigateToStockIn = { navController.navigate(Screen.StockIn.createRoute()) },
                    onNavigateToStockOut = { navController.navigate(Screen.StockOut.createRoute()) },
                    onNavigateToProductDetail = { id ->
                        navController.navigate(Screen.ProductDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.ProductList.route) {
                ProductListScreen(
                    factory = factory,
                    onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                    onAddProductClick = { navController.navigate(Screen.AddEditProduct.createRoute()) }
                )
            }

            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                ProductDetailScreen(
                    productId = productId,
                    factory = factory,
                    onBack = { navController.popBackStack() },
                    onEditClick = { navController.navigate(Screen.AddEditProduct.createRoute(productId)) },
                    onStockInClick = { navController.navigate(Screen.StockIn.createRoute(productId)) },
                    onStockOutClick = { navController.navigate(Screen.StockOut.createRoute(productId)) },
                    onDeleted = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.AddEditProduct.route,
                arguments = listOf(navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: -1L
                AddEditProductScreen(
                    productId = if (productId == -1L) null else productId,
                    factory = factory,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.StockIn.route,
                arguments = listOf(navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: -1L
                StockInScreen(
                    preselectedProductId = if (productId == -1L) null else productId,
                    factory = factory,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.StockOut.route,
                arguments = listOf(navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: -1L
                StockOutScreen(
                    preselectedProductId = if (productId == -1L) null else productId,
                    factory = factory,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(Screen.TransactionHistory.route) {
                TransactionHistoryScreen(factory = factory)
            }

            composable(Screen.Report.route) {
                ReportScreen(factory = factory)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(onNavigateToReport = { navController.navigate(Screen.Report.route) })
            }
        }
    }
}
