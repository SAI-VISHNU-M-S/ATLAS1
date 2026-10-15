package com.yourcompany.atlas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yourcompany.atlas.data.AppDatabase
import com.yourcompany.atlas.data.MemoryEntity
import com.yourcompany.atlas.ui.MapScreen
import com.yourcompany.atlas.ui.MemoryDetailScreen
import com.yourcompany.atlas.ui.theme.ATLASTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val dao = db.memoryDao()

        setContent {
            ATLASTheme {
                AppNavigation(dao = dao, lifecycleScope = lifecycleScope)
            }
        }
    }
}

@Composable
fun AppNavigation(
    dao: com.yourcompany.atlas.data.MemoryDao,
    lifecycleScope: kotlinx.coroutines.CoroutineScope
) {
    val navController = rememberNavController()
    val memories by dao.getAllMemories().collectAsState(initial = emptyList())
    var currentMemory by remember { mutableStateOf<MemoryEntity?>(null) }

    NavHost(navController = navController, startDestination = "map") {
        composable("map") {
            MapScreen(
                memories = memories,
                onLocationSelected = { lat, lng, id ->
                    lifecycleScope.launch {
                        val existing = id?.let { dao.getMemoryById(it) }
                            ?: dao.getMemoryByLocation(lat, lng)
                            ?: MemoryEntity(latitude = lat, longitude = lng)
                        currentMemory = existing
                        navController.navigate("detail")
                    }
                }
            )
        }
        composable("detail") {
            currentMemory?.let { memory ->
                MemoryDetailScreen(
                    memory = memory,
                    onSave = { updatedMemory ->
                        lifecycleScope.launch {
                            dao.insertOrUpdate(updatedMemory)
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}