package me.pog5.andromeda

import io.papermc.paper.command.brigadier.CommandSourceStack
import me.pog5.andromeda.commands.AndromedaCommand
import me.pog5.andromeda.commands.base.HelpCommand
import me.pog5.andromeda.commands.base.WhisperCommand
import me.pog5.andromeda.listener.PlayerEventListeners
import me.pog5.andromeda.managers.*
import me.pog5.andromeda.util.Formatting
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler
import org.incendo.cloud.paper.PaperCommandManager

@Suppress("UnstableApiUsage")
class Andromeda : JavaPlugin() {
    // Managers
    lateinit var userManager: UserManager
    var configManager: ConfigManager = ConfigManager(this)

    // Cloud Command Framework
    val commandManager: PaperCommandManager<CommandSourceStack> = PaperCommandManager.builder()
        .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
        .buildOnEnable(this)

    override fun onEnable() {
        val db = activateDatabase(configManager.getConfig())
        userManager = UserManager(this, Database(db))
        setupCloud() // Register Cloud Commands

        server.pluginManager.registerEvents(PlayerEventListeners(this), this)
        server.onlinePlayers.forEach { player ->
            userManager.addPlayer(player)
            // Send a welcome message
            player.sendActionBar(
                Formatting().mm("<italic>Welcome to</italic> <bold><gradient:#5e4fa2:#f79459>Andromeda!</gradient></bold>")
            )
        }
    }

    override fun onDisable() {
        HandlerList.unregisterAll(this)
    }

    fun activateDatabase(config: Config): org.jetbrains.exposed.sql.Database {
        when(configManager.getConfig().databaseType) {
            DatabaseType.SQLITE -> {
                val db = org.jetbrains.exposed.sql.Database.connect(
                    "jdbc:sqlite:${dataFolder}/" + configManager.getConfig().sqliteConfig?.filePath!!
                )
                return db
            }
            DatabaseType.POSTGRES -> {
                val db = org.jetbrains.exposed.sql.Database.connect(
                    url = "jdbc:postgresql://" +
                            configManager.getConfig().postgresConfig?.host!! + ":" +
                            configManager.getConfig().postgresConfig?.port!! + "/" +
                            configManager.getConfig().postgresConfig?.database!!,
                    user = configManager.getConfig().postgresConfig?.username!!,
                    password = configManager.getConfig().postgresConfig?.password!!
                )
                return db
            }
            else -> {
                logger.severe("Invalid database type in config.yml")
                server.pluginManager.disablePlugin(this)
                throw Exception("Invalid database type in config.yml")
            }
        }
    }

    fun setupCloud() {
        // Exception Handler
        MinecraftExceptionHandler.create(CommandSourceStack::getSender)

        // Basic Commands
        val basicCommands = setOf<AndromedaCommand>(HelpCommand(), WhisperCommand())
        basicCommands.forEach { cmd -> cmd.implement(this) }


    }

}