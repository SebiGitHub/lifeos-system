package com.sebi.lifeos.lifeosapp.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sebi.lifeos.lifeosapp.ui.components.TabIcon

@Composable
fun MainScaffold(
    modifier: Modifier = Modifier,
    content: @Composable (BottomTab) -> Unit
) {
    var tab by rememberSaveable { mutableStateOf(BottomTab.TODAY) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                BottomTab.entries.forEach { t ->
                    val selected = tab == t
                    NavigationBarItem(
                        selected = selected,
                        onClick = { tab = t },
                        icon = { TabIcon(tab = t, selected = selected) },
                        label = { Text(t.label) },
                        // ✅ quitamos el “indicator” estándar para que no interfiera con nuestro fondo
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { inner ->
        val animMs = 460

        AnimatedContent(
            targetState = tab,
            label = "tabAnim_strong",
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                val dir = if (forward) 1 else -1

                (slideInHorizontally(
                    animationSpec = tween(animMs, easing = FastOutSlowInEasing),
                    initialOffsetX = { it * dir }      // ✅ más desplazamiento
                ) + fadeIn(animationSpec = tween(animMs)) +
                        scaleIn(animationSpec = tween(animMs), initialScale = 0.94f))
                    .togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(animMs, easing = FastOutSlowInEasing),
                            targetOffsetX = { -it * dir }
                        ) + fadeOut(animationSpec = tween(animMs)) +
                                scaleOut(animationSpec = tween(animMs), targetScale = 1.04f)
                    )
            }
        ) { current ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
            ) {
                content(current)
            }
        }
    }
}

@Composable
fun MainScaffold(
    modifier: Modifier = Modifier,
    content: @Composable (BottomTab, PaddingValues) -> Unit
) {
    var tab by rememberSaveable { mutableStateOf(BottomTab.TODAY) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                BottomTab.entries.forEach { t ->
                    val selected = tab == t
                    NavigationBarItem(
                        selected = selected,
                        onClick = { tab = t },
                        icon = { TabIcon(tab = t, selected = selected) },
                        label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { inner ->
        val animMs = 460

        AnimatedContent(
            targetState = tab,
            label = "tabAnim_strong_pad",
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                val dir = if (forward) 1 else -1

                (slideInHorizontally(
                    animationSpec = tween(animMs, easing = FastOutSlowInEasing),
                    initialOffsetX = { it * dir }
                ) + fadeIn(animationSpec = tween(animMs)) +
                        scaleIn(animationSpec = tween(animMs), initialScale = 0.94f))
                    .togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(animMs, easing = FastOutSlowInEasing),
                            targetOffsetX = { -it * dir }
                        ) + fadeOut(animationSpec = tween(animMs)) +
                                scaleOut(animationSpec = tween(animMs), targetScale = 1.04f)
                    )
            }
        ) { current ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
            ) {
                content(current, inner)
            }
        }
    }
}