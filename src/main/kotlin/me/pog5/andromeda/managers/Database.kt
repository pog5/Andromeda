package me.pog5.andromeda.managers

import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class Database(private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(PersistentDataEntity)
        }
    }

    fun loadPlayerData(player: Player): PersistentData? {
        TODO()
    }

    fun savePlayerData(uuid: UUID, data: PersistentData) {
        TODO()
    }
}