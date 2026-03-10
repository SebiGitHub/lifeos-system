package com.sebi.lifeos.lifeosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sebi.lifeos.lifeosapp.data.LifeOsDb
import com.sebi.lifeos.lifeosapp.repo.UsageRepository
import com.sebi.lifeos.lifeosapp.ui.AppCatalogScreen
import com.sebi.lifeos.lifeosapp.ui.AppCatalogViewModel
import com.sebi.lifeos.lifeosapp.ui.AppCatalogVmFactory
import com.sebi.lifeos.lifeosapp.ui.BottomTab
import com.sebi.lifeos.lifeosapp.ui.MainScaffold
import com.sebi.lifeos.lifeosapp.ui.RankingScreen
import com.sebi.lifeos.lifeosapp.ui.RankingViewModel
import com.sebi.lifeos.lifeosapp.ui.RankingVmFactory
import com.sebi.lifeos.lifeosapp.ui.SettingsScreen
import com.sebi.lifeos.lifeosapp.ui.TodayScreen
import com.sebi.lifeos.lifeosapp.ui.TodayViewModel
import com.sebi.lifeos.lifeosapp.ui.TodayVmFactory
import com.sebi.lifeos.lifeosapp.ui.YearScreen
import com.sebi.lifeos.lifeosapp.ui.YearViewModel
import com.sebi.lifeos.lifeosapp.ui.YearVmFactory
import com.sebi.lifeos.lifeosapp.ui.components.rememberVm
import com.sebi.lifeos.lifeosapp.ui.theme.LifeOsAppTheme
import com.sebi.lifeos.lifeosapp.usage.UsageAccess
import com.sebi.lifeos.lifeosapp.util.AppPrefs
import com.sebi.lifeos.lifeosapp.worker.DailyUsageScheduler

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val ctx = this

            val db = remember { LifeOsDb.get(applicationContext) }
            val repo = remember { UsageRepository(applicationContext, db) }

            var darkMode by remember { mutableStateOf(AppPrefs.isDarkMode(ctx)) }
            var realtime by remember { mutableStateOf(AppPrefs.isRealtimeTrackingEnabled(ctx)) }

            LifeOsAppTheme(darkTheme = darkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // IMPORTANTE: usa el overload con 1 parámetro -> MainScaffold aplica el padding
                    MainScaffold { tab ->
                        when (tab) {
                            BottomTab.TODAY -> {
                                val vm: TodayViewModel = rememberVm(factory = TodayVmFactory(repo))
                                TodayScreen(vm = vm, modifier = Modifier.fillMaxSize())
                            }

                            BottomTab.YEAR -> {
                                val vm: YearViewModel = rememberVm(factory = YearVmFactory(repo))
                                YearScreen(vm = vm, modifier = Modifier.fillMaxSize())
                            }

                            BottomTab.CATALOG -> {
                                val vm: AppCatalogViewModel = rememberVm(factory = AppCatalogVmFactory(repo))
                                AppCatalogScreen(vm = vm, modifier = Modifier.fillMaxSize())
                            }

                            BottomTab.RANKING -> {
                                val vm: RankingViewModel = rememberVm(factory = RankingVmFactory(repo))
                                RankingScreen(vm = vm, modifier = Modifier.fillMaxSize())
                            }

                            BottomTab.SETTINGS -> {
                                SettingsScreen(
                                    darkMode = darkMode,
                                    onDarkModeChange = {
                                        darkMode = it
                                        AppPrefs.setDarkMode(ctx, it)
                                    },
                                    realtimeTracking = realtime,
                                    onRealtimeTrackingChange = {
                                        realtime = it
                                        AppPrefs.setRealtimeTrackingEnabled(ctx, it)
                                        DailyUsageScheduler.setRealtimeEnabled(ctx, it)
                                    },
                                    onOpenUsageAccess = { UsageAccess.openUsageAccessSettings(ctx) },
                                    onSyncNow = { DailyUsageScheduler.refreshTodayNow(ctx) },
                                    onRunDailyCaptureNow = { DailyUsageScheduler.runDailyCaptureNow(ctx) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
