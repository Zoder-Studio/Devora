package dev.devora.feature.pluginsystem.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.devora.feature.pluginsystem.api.DevoraPlugin
import dev.devora.feature.pluginsystem.domain.PluginRegistry
import javax.inject.Inject

@HiltViewModel
class PluginListViewModel @Inject constructor(
    private val registry: PluginRegistry
) : ViewModel() {
    val plugins: List<DevoraPlugin> = registry.allPlugins()
}