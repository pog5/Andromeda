package me.pog5.andromeda.listener

import me.pog5.andromeda.Andromeda
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerEventListeners(private val plugin: Andromeda) : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        plugin.userManager.addPlayer(player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        plugin.userManager.removePlayer(player)
    }
}