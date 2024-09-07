package me.pog5.andromeda.managers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.pog5.andromeda.Andromeda
import me.pog5.andromeda.util.SUUID
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.PlayerInventory
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.postgresql.util.PGobject
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class User(
    val player: Player,
    var session: SessionData,
    var data: PersistentData,
) {
    fun sendMessage(message: Component) {
        player.sendMessage(message)
    }
    fun sendMessage(message: String) = sendMessage(Component.text(message))
}

data class SessionData(
    var spectatorState: Boolean = true,
    var currentWorld: World? = null,
)

@Serializable
data class PersistentData(
    var identity: Identity? = null,
    val user: PDUser = PDUser(),
    val pvp: PDPvP = PDPvP(),
)

object PersistentDataEntity : Table() {
    val uuid = uuid("uuid")
    val data = jsonb("data", PersistentData.serializer())

    fun Table.jsonb(name: String, serializer: KSerializer<*>): Column<Any> =
        registerColumn<Any>(name, object : ColumnType() {
            override var nullable: Boolean = false

            override fun sqlType(): String = "jsonb"

            override fun valueFromDB(value: Any): Any {
                val json = if (value is PGobject) value.value else value.toString()
                return Json.decodeFromString(serializer, json!!)!!
            }

            override fun notNullValueToDB(value: Any): Any =
                PGobject().apply {
                    this.type = "jsonb"
                    @Suppress("UNCHECKED_CAST")
                    this.value = Json.encodeToString(serializer as KSerializer<Any>, value)
                }
        })
}

@Serializable
@JvmInline
value class Nick(val value: String?)

@JvmInline
@Serializable
value class RealName(val value: String = "Unknown")

@JvmInline
@Serializable
value class Skin(val value: String?)

@Serializable
data class PDUser(
    val nick: Nick? = null,
    val realName: RealName = RealName(),
) {
    val name: String get() = nick?.value ?: realName.value
}

@Serializable
data class Identity(
    val uuid: SUUID,
    val name: String,
    val skin: Skin? = null,
)

@Serializable
data class PDPvP(
    var kills: Int = 0,
    var deaths: Int = 0,
    var savedKits: HashMap<String, PlayerInventory> = hashMapOf(),
    private var _killStreak: Int = 0,
    var highestKillStreak: Int = 0,
    var lastKilledBy: String = "",
    var lastKilledAt: Long = 0,
) {
    val kdr: Double get() = if (deaths == 0) kills.toDouble() else kills.toDouble() / deaths
    var killStreak: Int
        get() = _killStreak
        set(value) {
            _killStreak = value
            if (value > highestKillStreak) {
                highestKillStreak = value
            }
        }
}

class UserManager(private val plugin: Andromeda, private val database: Database) {
    private val activePlayers: MutableMap<UUID, User> = ConcurrentHashMap()

    // When a player joins, load both session data and persistent data
    fun addPlayer(player: Player) {
        val persistentData = database.loadPlayerData(player.uniqueId)
        if (persistentData == null) {
            player.kick(
                Component.text("Failed to load player data").color(
                    TextColor.color(0xea76cb)
                )
            )
            throw IllegalStateException("Failed to load player data for player: ${player.uniqueId} (${player.name})")
        }
        activePlayers[player.uniqueId] = User(player, session = SessionData(), data = persistentData)
    }

    // When a player leaves, save their persistent data and remove from active players
    fun removePlayer(player: Player) {
        activePlayers.remove(player.uniqueId)?.let {
            database.savePlayerData(player.uniqueId, it.data)
        }
    }

    // Retrieve User from UUID
    fun getPlayerFromUUID(uuid: UUID): User? = activePlayers[uuid]

    // Retrieve User from Name
    fun getPlayer(name: String): User? = activePlayers[plugin.server.getPlayer(name)?.uniqueId]

    // Retrieve all Users
    fun getOnlineUsers(): Collection<User> = activePlayers.values

    fun getOnlineUUIDs(): Set<UUID> = activePlayers.keys

    fun getOnlineNames(ignoreDisguises: Boolean = false): Set<String> =
        activePlayers.values.mapTo(HashSet()) { it.player.name }.toSet()

    // Example of event-related method (e.g., handling a player's death)
    fun handlePlayerDeath(player: Player) {
        getPlayerFromUUID(player.uniqueId)?.let {
            it.data.pvp.deaths++
//            plugin.logger.info("${player.name} died to ${player.lastDamageCause}")
        }
    }
}