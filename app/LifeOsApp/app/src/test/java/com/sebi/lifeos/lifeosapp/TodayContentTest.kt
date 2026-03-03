package com.sebi.lifeos.lifeosapp

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.sebi.lifeos.lifeosapp.data.AppUsageAgg
import com.sebi.lifeos.lifeosapp.ui.TodayContent
import com.sebi.lifeos.lifeosapp.ui.TodayUiState
import com.sebi.lifeos.lifeosapp.ui.theme.LifeOsAppTheme
import org.junit.Rule
import org.junit.Test

class TodayContentTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun showsPermissionMessageWhenNoAccess() {
        rule.setContent {
            LifeOsAppTheme {
                TodayContent(
                    state = TodayUiState(hasUsagePermission = false, topApps = emptyList()),
                    onRefresh = {},
                    onMoveSelection = {},
                    onSelectIndex = {},
                    onRequestUsagePermission = {},
                    onSyncNow = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        rule.onNodeWithText("Necesitas conceder \"Acceso a uso\" para ver el tracking.").assertExists()
    }

    @Test
    fun showsTopAppLabelWhenDataExists() {
        rule.setContent {
            LifeOsAppTheme {
                TodayContent(
                    state = TodayUiState(
                        hasUsagePermission = true,
                        dayKeys = listOf("2026-02-01"),
                        dayTotalsMs = listOf(10_000L),
                        selectedIndex = 0,
                        topApps = listOf(
                            AppUsageAgg(
                                packageName = "com.google.android.youtube",
                                label = "YouTube",
                                totalMs = 600_000L
                            )
                        )
                    ),
                    onRefresh = {},
                    onMoveSelection = {},
                    onSelectIndex = {},
                    onRequestUsagePermission = {},
                    onSyncNow = {},
                    snackbarHostState = SnackbarHostState()
                )
            }
        }

        rule.onNodeWithText("YouTube").assertExists()
    }
}
