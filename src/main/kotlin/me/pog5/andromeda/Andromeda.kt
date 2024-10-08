package me.pog5.andromeda

import io.papermc.paper.command.brigadier.CommandSourceStack
import me.pog5.andromeda.commands.base.HelpCommand
import me.pog5.andromeda.commands.base.WhisperCommand
import me.pog5.andromeda.listener.PlayerEventListeners
import me.pog5.andromeda.managers.ConfigManager
import me.pog5.andromeda.managers.UserManager
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
        userManager = UserManager(this, configManager.getConfig().database)
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

    fun setupCloud() {
        // Exception Handler
        MinecraftExceptionHandler.create(CommandSourceStack::getSender)

        // Basic Commands
        val basicCommands = setOf(HelpCommand(), WhisperCommand())
        basicCommands.forEach { cmd -> cmd.implement(this) }
    }

    fun bye() {
        onDisable()
        server.pluginManager.disablePlugin(this)
    }

}