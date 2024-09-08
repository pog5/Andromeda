package me.pog5.andromeda.util

import org.bukkit.configuration.file.FileConfiguration

class ConfigReader(private val config: FileConfiguration) {
    fun getString(path: String): String? = config.getString(path)
    fun getInt(path: String): Int? = config.getInt(path, -6969969).let { if (it == -6969969) null else it }
    fun getDouble(path: String): Double? = config.getDouble(path, -6960.69).let { if (it == -6960.69) null else it }
    fun getBoolean(path: String): Boolean? = config.get(path, "not a boolean") as? Boolean
}