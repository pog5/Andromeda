package me.pog5.andromeda.commands

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import me.pog5.andromeda.Andromeda
import org.incendo.cloud.paper.PaperCommandManager

fun interface AndromedaCommand {
    fun implement(plugin: Andromeda);
}