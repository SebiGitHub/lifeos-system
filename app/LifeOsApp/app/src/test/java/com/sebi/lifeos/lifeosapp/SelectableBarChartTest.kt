package com.sebi.lifeos.lifeosapp

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sebi.lifeos.lifeosapp.ui.components.SelectableBarChart
import com.sebi.lifeos.lifeosapp.ui.theme.LifeOsAppTheme
import org.junit.Rule
import org.junit.Test

class SelectableBarChartTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun clickingLabelChangesSelectedIndex() {
        rule.setContent {
            LifeOsAppTheme {
                var selected by mutableIntStateOf(0)
                Column {
                    SelectableBarChart(
                        valuesMs = listOf(0L, 1000L, 2000L),
                        labels = listOf("A", "B", "C"),
                        selectedIndex = selected,
                        onSelect = { selected = it }
                    )
                    Text("selected=$selected")
                }
            }
        }

        rule.onNodeWithText("B").performClick()
        rule.onNodeWithText("selected=1").assertExists()
    }
}
