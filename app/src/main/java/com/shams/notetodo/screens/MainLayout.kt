package com.shams.notetodo.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainLayout(
    showTopBar: Boolean = true,
    topBarContent: @Composable () -> Unit = { DefaultHeader() },
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            if (showTopBar) {
                topBarContent()
            }
        },
        content = content
    )
}