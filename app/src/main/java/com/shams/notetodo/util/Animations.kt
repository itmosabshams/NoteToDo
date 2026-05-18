package com.shams.notetodo.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*

object Animations {

    // انیمیشن باز شدن صفحه (بزرگ شدن از مرکز)
    val enterTransition = scaleIn(
        initialScale = 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
    ) + fadeIn(
        animationSpec = tween(durationMillis = 350)
    )

    // انیمیشن بسته شدن صفحه (کوچک شدن به مرکز)
    val exitTransition = scaleOut(
        targetScale = 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
    ) + fadeOut(
        animationSpec = tween(durationMillis = 300)
    )

    val popEnterTransition = fadeIn(
        animationSpec = tween(durationMillis = 300)
    )

    val popExitTransition = fadeOut(
        animationSpec = tween(durationMillis = 250)
    )
}