package com.reelguard.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reelguard.app.BuildConfig
import com.reelguard.app.data.model.Platform

/**
 * Step 1 placeholder UI: just one switch per platform, backed by Room,
 * so we can verify the data layer persists. The real settings UI comes in Step 4.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val byPlatform = settings.associateBy { it.platform }

    Scaffold(topBar = { TopAppBar(title = { Text("ReelGuard") }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item { ServiceStatusCard() }
            if (BuildConfig.DEBUG) item { DebugCard() }
            items(Platform.entries) { platform ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(platform.label)
                    Switch(
                        checked = byPlatform[platform.name]?.blockFeed ?: false,
                        onCheckedChange = { viewModel.setBlockFeed(platform, it) },
                    )
                }
                HorizontalDivider()
            }
        }
    }
}
