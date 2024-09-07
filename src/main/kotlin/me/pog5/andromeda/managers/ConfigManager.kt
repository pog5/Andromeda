package me.pog5.andromeda.managers

import me.pog5.andromeda.Andromeda

data class Config(
    val serverName: String,
    val modules: Modules,
    val databaseType: DatabaseType = DatabaseType.SQLITE,
    val sqliteConfig: SQLiteConfig?,
    val postgresConfig: PostgresConfig?,
)

data class Modules (
    val pvp: Boolean,
)

enum class DatabaseType {
    SQLITE,
    POSTGRES,
}

data class SQLiteConfig(
    val filePath: String,
)

data class PostgresConfig(
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
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
            ),
            databaseType = DatabaseType.valueOf(fileConfig.getString("database.type") ?: "SQLITE"),
            sqliteConfig = SQLiteConfig(
                filePath = fileConfig.getString("database.sqlite.file-path") ?: "data.db"
            ),
            postgresConfig = PostgresConfig(
                host = fileConfig.getString("database.postgres.host") ?: "localhost",
                port = fileConfig.getInt("database.postgres.port"),
                database = fileConfig.getString("database.postgres.database") ?: "andromeda",
                username = fileConfig.getString("database.postgres.username") ?: "root",
                password = fileConfig.getString("database.postgres.password") ?: "password"
            )
        )
    }

    fun getConfig(): Config {
        return serverConfig
    }
}