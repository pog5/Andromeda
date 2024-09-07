package me.pog5.andromeda.commands.base

import me.pog5.andromeda.Andromeda
import me.pog5.andromeda.commands.AndromedaCommand
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser
import org.incendo.cloud.description.Description
import org.incendo.cloud.parser.standard.StringParser
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider


class WhisperCommand: AndromedaCommand {
    override fun implement(plugin: Andromeda) {
        val commandManager = plugin.commandManager
        val userManager = plugin.userManager

        commandManager.command(commandManager.commandBuilder("/w", "/msg", "/whisper")
            .required("target",
                SinglePlayerSelectorParser.singlePlayerSelectorParser(),
                Description.description("Who you want to private message"),
                SuggestionProvider.blocking { _, input ->
                    userManager.getOnlineNames(false).filter { it.startsWith(input.input()) }.map(Suggestion::suggestion)
                }
            )
            .required("message",
                StringParser.greedyStringParser(),
                Description.description("The message you want to send"),
            )
            .handler { context ->
                val sender = userManager.getPlayer(context.sender().sender.name)
                val target = userManager.getPlayer(context["target"])
                val message: String = context["message"]

                if (target != null) {
                    sender?.player?.sendMessage("You whispered to ${target.data.user.name}: $message")
                    target.player.sendMessage("${target.data.user.name} whispered to you: $message")
                } else {
                    sender.sendMessage("Player not found")
                }
            }
        )
    }
}