package me.pog5.andromeda.managers

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import me.pog5.andromeda.Andromeda
import java.io.File

@Serializable
data class Config(
    val serverName: String = "<UNKNOWN>",
    val modules: Modules = Modules(),
    val database: DatabaseConfig = DatabaseConfig.SQLite(),
)

@Serializable
data class Modules(
    val pvp: Boolean = false,
)

class ConfigManager(private val plugin: Andromeda) {
    private val configFile = File(plugin.dataFolder, "config.yml")
    private var config: Config = loadConfig()

    fun reloadConfig(): Config {
        config = loadConfig()
        return config
    }

    fun getConfig(): Config {
        return config
    }

    private fun loadConfig(): Config {
        return if (configFile.exists()) {
            try {
                Yaml.default.decodeFromString(configFile.readText())
            } catch (e: SerializationException) {
                plugin.logger.severe("Failed to decode config.yml")
                e.printStackTrace()
                plugin.bye()
                throw e
            } catch (e: IllegalArgumentException) {
                plugin.logger.severe("You have an invalid config.yml, fix it or delete it and i'll generate a new one")
                e.printStackTrace()
                plugin.bye()
                throw e
            }
        } else {
            plugin.dataFolder.mkdirs()
            configFile.createNewFile()
            val defaultConfig = Config()
            configFile.writeText(Yaml.default.encodeToString(Config.serializer(), defaultConfig))
            defaultConfig
        }
    }
}