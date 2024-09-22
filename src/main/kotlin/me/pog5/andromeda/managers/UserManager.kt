package me.pog5.andromeda.managers

import kotlinx.serialization.Serializable
import me.pog5.andromeda.Andromeda
import me.pog5.andromeda.util.SUUID
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.PlayerInventory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class User(
    val player: Player,
    var session: SessionData,
    var data: PersistentData,
) {
    fun sendMessage(message: Component) = player.sendMessage(message)
    fun sendMessage(message: String) = sendMessage(Component.text(message))
}

data class SessionData(
    var spectatorState: Boolean = false,
    var combatTicksLeft: Long = -1,
    var damageLog: HashMap<User, Double> = hashMapOf(),
)

@Serializable
data class PersistentData(
    var identity: Identity? = null,
    val user: PDUser = PDUser(),
    val pvp: PDPvP = PDPvP(),
)

@JvmInline
@Serializable
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
    val savedKits: MutableMap<String, PlayerInventory> = hashMapOf(),
    private var _killStreak: Int = 0,
    var highestKillStreak: Int = 0,
) {
    val kdr: Double get() = if (deaths == 0) kills.toDouble() else kills.toDouble() / deaths
    var killStreak: Int
        get() = _killStreak
        set(value) {
            _killStreak = value
            highestKillStreak = maxOf(highestKillStreak, value)
        }
}

class UserManager(private val plugin: Andromeda, private val database: DatabaseConfig) {
    private val activePlayers: MutableMap<UUID, User> = ConcurrentHashMap()

    fun addPlayer(player: Player) {
        val persistentData = database.loadPlayerData(player)
        activePlayers[player.uniqueId] = User(player, SessionData(), persistentData)
    }

    fun removePlayer(player: Player) {
        activePlayers.remove(player.uniqueId)?.let {
            database.savePlayerData(player, it.data)
        }
    }

    fun getPlayerFromUUID(uuid: UUID): User? = activePlayers[uuid]

    fun getPlayer(name: String): User? = plugin.server.getPlayer(name)?.uniqueId?.let { activePlayers[it] }

    fun getOnlineUsers(): Collection<User> = activePlayers.values

    fun getOnlineUUIDs(): Set<UUID> = activePlayers.keys

    fun getOnlineNames(ignoreDisguises: Boolean = false): Set<String> =
        activePlayers.values.mapTo(HashSet()) { it.player.name }

    fun handlePlayerDeath(player: Player) {
        getPlayerFromUUID(player.uniqueId)?.data?.pvp?.deaths?.plus(1)
    }
}