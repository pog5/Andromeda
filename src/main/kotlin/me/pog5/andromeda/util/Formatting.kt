package me.pog5.andromeda.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

class Formatting {
    fun mm(miniMessageString: String): Component {
        return MiniMessage.miniMessage().deserialize(miniMessageString)
    }

    fun str(component: Component): String {
        return MiniMessage.miniMessage().serialize(component)
    }
}