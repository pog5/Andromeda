package me.pog5.andromeda.managers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class Database(private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(PersistentDataEntity)
        }
    }

    fun loadPlayerData(uuid: UUID): PersistentData? {
        return transaction(database) {
            PersistentDataEntity.select { PersistentDataEntity.uuid eq uuid }
                .firstOrNull()?.let {
                    Json.decodeFromString<PersistentData>(it[PersistentDataEntity.data] as String)
                }
        }
    }

    fun savePlayerData(uuid: UUID, data: PersistentData) {
        val jsonData = Json.encodeToString(data)
        transaction(database) {
            // Check if a row with the given UUID exists
            val existingRow = PersistentDataEntity.select {
                PersistentDataEntity.uuid eq uuid
            }.firstOrNull()

            if (existingRow != null) {
                // Update the existing row
                PersistentDataEntity.update({ PersistentDataEntity.uuid eq uuid }) {
                    it[this.data] = jsonData
                }
            } else {
                // Insert a new row
                PersistentDataEntity.insert {
                    it[this.uuid] = uuid
                    it[this.data] = jsonData
                }
            }
        }
    }
}