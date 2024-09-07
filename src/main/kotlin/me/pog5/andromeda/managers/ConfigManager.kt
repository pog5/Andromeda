package me.pog5.andromeda.managers

import me.pog5.andromeda.Andromeda

data class Config(
    val serverName: String,
    val modules: Modules,
)

data class Modules (
    val pvp: Boolean,
)

class ConfigManager(private val plugin: Andromeda) {
    private var serverConfig: Config = reloadConfig()

    fun reloadConfig(): Config {
        // Read the plugin's config.yml file, create if it doesn't exist
        plugin.saveDefaultConfig()
        val fileConfig = plugin.config

        // Make a new config and return it
        return Config(
            // Either get the value from the config file or use the default value of the data class above
            serverName = fileConfig.getString("server-name") ?: "<UNKNOWN>",
            modules = Modules(
                pvp = fileConfig.getBoolean("modules.pvp")
            )
        )
    }

    fun getConfig(): Config {
        return serverConfig
    }
}