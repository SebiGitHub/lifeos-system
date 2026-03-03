package com.sebi.lifeos.lifeosapp.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.sebi.lifeos.lifeosapp.repo.UsageRepository
import kotlin.jvm.functions.Function0

private fun appContextFrom(extras: CreationExtras): Context {
    val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
        ?: error("No Application en CreationExtras")
    return app.applicationContext
}

private fun <VM : ViewModel> instantiateWithRepoAndMaybeContext(
    cls: Class<VM>,
    ctx: Context,
    repo: UsageRepository
): VM {
    // (repo)
    runCatching {
        cls.getConstructor(UsageRepository::class.java).newInstance(repo)
    }.getOrNull()?.let { return it }

    // (context, repo)
    runCatching {
        cls.getConstructor(Context::class.java, UsageRepository::class.java).newInstance(ctx, repo)
    }.getOrNull()?.let { return it }

    // (repo, contextProvider)
    runCatching {
        val ctor = cls.getConstructor(UsageRepository::class.java, Function0::class.java)
        val provider = object : Function0<Context> { override fun invoke(): Context = ctx }
        ctor.newInstance(repo, provider)
    }.getOrNull()?.let { return it }

    error(
        "Constructor no compatible en ${cls.name}. " +
                "Soportados: (repo) o (context, repo) o (repo, contextProvider)."
    )
}

class TodayVmFactory(private val repo: UsageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val ctx = appContextFrom(extras)
        if (modelClass.isAssignableFrom(TodayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return instantiateWithRepoAndMaybeContext(TodayViewModel::class.java, ctx, repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class YearVmFactory(private val repo: UsageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val ctx = appContextFrom(extras)
        if (modelClass.isAssignableFrom(YearViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return instantiateWithRepoAndMaybeContext(YearViewModel::class.java, ctx, repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class AppCatalogVmFactory(private val repo: UsageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val ctx = appContextFrom(extras)
        if (modelClass.isAssignableFrom(AppCatalogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return instantiateWithRepoAndMaybeContext(AppCatalogViewModel::class.java, ctx, repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class RankingVmFactory(private val repo: UsageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val ctx = appContextFrom(extras)
        if (modelClass.isAssignableFrom(RankingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return instantiateWithRepoAndMaybeContext(RankingViewModel::class.java, ctx, repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
