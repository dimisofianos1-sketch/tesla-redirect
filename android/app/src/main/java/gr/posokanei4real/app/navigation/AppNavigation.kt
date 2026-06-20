package gr.posokanei4real.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import gr.posokanei4real.app.ui.catalog.CatalogScreen
import gr.posokanei4real.app.ui.detail.ItemDetailScreen

private const val ROUTE_CATALOG = "catalog"
private const val ROUTE_DETAIL = "detail/{itemId}"

@Composable
fun AppNavigation() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = ROUTE_CATALOG) {
        composable(ROUTE_CATALOG) {
            CatalogScreen(
                onItemClick = { itemId ->
                    nav.navigate("detail/$itemId")
                },
            )
        }
        composable(
            route = ROUTE_DETAIL,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
        ) {
            ItemDetailScreen(onBack = { nav.popBackStack() })
        }
    }
}
