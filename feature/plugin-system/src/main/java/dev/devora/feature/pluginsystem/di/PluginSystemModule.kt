package dev.devora.feature.pluginsystem.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.devora.feature.pluginsystem.api.DevoraPlugin
import dev.devora.feature.pluginsystem.builtin.GitHubWorkflowsPlugin
import dev.devora.feature.pluginsystem.data.DefaultPluginRegistry
import dev.devora.feature.pluginsystem.domain.PluginRegistry
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PluginSystemModule {

    /**
     * The full list of plugins bundled with this Devora build. To add
     * a new plugin: implement DevoraPlugin (or WorkflowYamlToolingPlugin)
     * in a new module, add it to this list, and add its module as a
     * dependency of :feature:plugin-system.
     */
    @Provides
    @Singleton
    fun providePlugins(): List<DevoraPlugin> = listOf(
        GitHubWorkflowsPlugin()
    )

    @Provides
    @Singleton
    fun providePluginRegistry(plugins: List<@JvmSuppressWildcards DevoraPlugin>): PluginRegistry =
        DefaultPluginRegistry(plugins)

    @Provides
    fun provideValidateWorkflowYamlUseCase(registry: PluginRegistry) =
        dev.devora.feature.pluginsystem.domain.usecase.ValidateWorkflowYamlUseCase(registry)
}