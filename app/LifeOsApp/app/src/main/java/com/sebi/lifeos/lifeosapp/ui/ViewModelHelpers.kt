package com.sebi.lifeos.lifeosapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

@Composable
inline fun <reified VM : ViewModel> rememberVm(
    factory: ViewModelProvider.Factory,
    key: String? = null
): VM {
    val owner = LocalViewModelStoreOwner.current
        ?: error("No ViewModelStoreOwner (¿estás dentro de setContent?)")

    return remember(owner, factory, key) {
        val provider = ViewModelProvider(owner, factory)
        if (key == null) provider.get(VM::class.java) else provider.get(key, VM::class.java)
    }
}
