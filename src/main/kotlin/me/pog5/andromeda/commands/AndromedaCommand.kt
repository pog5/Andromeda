package me.pog5.andromeda.commands

import me.pog5.andromeda.Andromeda

fun interface AndromedaCommand {
    fun implement(plugin: Andromeda)
}