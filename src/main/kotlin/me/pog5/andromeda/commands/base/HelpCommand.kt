package me.pog5.andromeda.commands.base

import io.papermc.paper.command.brigadier.CommandSourceStack
import me.pog5.andromeda.Andromeda
import me.pog5.andromeda.commands.AndromedaCommand
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.help.result.CommandEntry
import org.incendo.cloud.minecraft.extras.MinecraftHelp
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.parser.standard.StringParser.greedyStringParser
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.Suggestion.suggestion
import org.incendo.cloud.suggestion.SuggestionProvider
import java.util.stream.Collectors

class HelpCommand: AndromedaCommand {
    override fun implement(plugin: Andromeda) {
        val commandManager = plugin.commandManager
        val help: MinecraftHelp<CommandSourceStack> = MinecraftHelp.create(
            "/help", commandManager
        ) { it.sender }
        commandManager.command(commandManager.commandBuilder("/help")
            .optional(
                "query",
                greedyStringParser(),
                DefaultValue.constant(""),
                SuggestionProvider.blocking { ctx, input ->
                    commandManager.createHelpHandler().queryRootIndex(ctx.sender()).entries().stream()
                        .map(CommandEntry<CommandSourceStack>::syntax).map(Suggestion::suggestion)
                        .collect(Collectors.toList())
                })
            .handler { context ->
                help.queryCommands(context["query"], context.sender())
            }
        )
    }
}