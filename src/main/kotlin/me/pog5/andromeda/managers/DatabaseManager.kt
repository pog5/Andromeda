package me.pog5.andromeda.managers

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import java.sql.DriverManager

val kickMessage = Component.text()
    .append(Component.text("Failed to load your data!", NamedTextColor.RED, TextDecoration.BOLD))
    .append(Component.newline())
    .append(Component.text("Try to rejoin, if that doesn't work then contact an admin.", NamedTextColor.GREEN))
    .append(Component.newline())
    .append(Component.text("Debug Info: ", NamedTextColor.LIGHT_PURPLE))
    .append(Component.newline())


@Serializable
sealed class DatabaseConfig {
    abstract val type: DatabaseType
    abstract fun loadPlayerData(player: Player): PersistentData
    abstract fun savePlayerData(player: Player, data: PersistentData)

    @Serializable
    data class SQLite(
        val filePath: String = "data.db",
    ) : DatabaseConfig() {
        override val type: DatabaseType = DatabaseType.SQLITE
        private val uri = "jdbc:sqlite:$filePath"
        val connection = DriverManager.getConnection(uri)
        override fun loadPlayerData(player: Player): PersistentData {
            val statement = connection.createStatement()
            statement.execute("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, data JSON)")
            val resultSet = statement.executeQuery("SELECT * FROM players WHERE uuid = '${player.uniqueId}'")
            if (resultSet.next()) {
                try {
                    return Json.decodeFromString<PersistentData>(resultSet.getString("data"))
                } catch (e: Exception) {
                    e.printStackTrace()
                    player.kick(kickMessage.append(Component.text(e.localizedMessage)).build())
                }
            }
            return PersistentData()
        }

        override fun savePlayerData(player: Player, data: PersistentData) {
            val statement = connection.createStatement()
            statement.execute("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, data JSON)")
            statement.execute("INSERT OR REPLACE INTO players (uuid, data) VALUES ('${player.uniqueId}', '${Json.encodeToString(PersistentData.serializer(), data)}')")
        }
    }

    @Serializable
    data class Postgres(
        val host: String = "localhost",
        val port: Int = 27017,
        val database: String = "andromeda",
        val username: String = "root",
        val password: String = "password"
    ) : DatabaseConfig() {
        override val type: DatabaseType = DatabaseType.POSTGRES
        override fun loadPlayerData(player: Player): PersistentData {
            val connection = DriverManager.getConnection("jdbc:postgresql://$host:$port/$database", username, password)
            val statement = connection.createStatement()
            statement.execute("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, data JSON)")
            val resultSet = statement.executeQuery("SELECT * FROM players WHERE uuid = '${player.uniqueId}'")
            if (resultSet.next()) {
                try {
                    return Json.decodeFromString<PersistentData>(resultSet.getString("data"))
                } catch (e: Exception) {
                    e.printStackTrace()
                    player.kick(kickMessage.append(Component.text(e.localizedMessage)).build())
                }
            }
            return PersistentData()
        }
        override fun savePlayerData(player: Player, data: PersistentData) {
            val connection = DriverManager.getConnection("jdbc:postgresql://$host:$port/$database", username, password)
            val statement = connection.createStatement()
            statement.execute("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, data JSON)")
            statement.execute("INSERT OR REPLACE INTO players (uuid, data) VALUES ('${player.uniqueId}', '${Json.encodeToString(PersistentData.serializer(), data)}')")
        }
    }

    @Serializable
    data class MongoDb(
        val connectionString: String = "mongodb://localhost:27017"
    ) : DatabaseConfig() {
        override val type: DatabaseType = DatabaseType.MONGODB
        override fun loadPlayerData(player: Player): PersistentData {
            val client = com.mongodb.client.MongoClients.create(connectionString)
            val database = client.getDatabase("andromeda")
            val collection = database.getCollection("players")
            val document = collection.find(org.bson.Document("uuid", player.uniqueId.toString())).first()
            if (document != null) {
                try {
                    return Json.decodeFromString<PersistentData>(document.getString("data"))
                } catch (e: Exception) {
                    e.printStackTrace()
                    player.kick(kickMessage.append(Component.text(e.localizedMessage)).build())
                }
            }
            return PersistentData()
        }
        override fun savePlayerData(player: Player, data: PersistentData) {
            val client = com.mongodb.client.MongoClients.create(connectionString)
            val database = client.getDatabase("andromeda")
            val collection = database.getCollection("players")
            collection.insertOne(org.bson.Document("uuid", player.uniqueId.toString()).append("data", Json.encodeToString(PersistentData.serializer(), data)))
        }
    }
}

enum class DatabaseType {
    SQLITE,
    POSTGRES,
    MONGODB,
}

class DatabaseManager