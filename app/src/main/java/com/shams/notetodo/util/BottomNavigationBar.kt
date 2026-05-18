package com.shams.notetodo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "tasks"

    val items = listOf(
        Triple("done", Icons.Default.Done, "انجام‌شده"),
        Triple("tasks", Icons.Default.Home, "تسک‌ها"),
        Triple("notes", Icons.Default.Notes, "یادداشت‌ها")
    )

    // فقط یک رنگ آبی روشن
    val blueColor = Color(0xFF03A9F4)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(blueColor)
                .shadow(8.dp, RoundedCornerShape(50))
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (route, icon, label) ->
                val selected = currentRoute == route

                Box(
                    modifier = Modifier
                        .size(if (selected) 44.dp else 38.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) Color.White.copy(alpha = 0.25f)
                            else Color.Transparent
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (!selected) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) Color.White else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(if (selected) 26.dp else 22.dp)
                    )
                }
            }
        }
    }
}