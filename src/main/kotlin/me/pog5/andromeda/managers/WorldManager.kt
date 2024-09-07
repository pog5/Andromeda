package me.pog5.andromeda.managers

import org.bukkit.Material

data class World(
    val name: String = "<PLAYER>'s Arena",
    val icon: Material = Material.GRASS_BLOCK,
    val owner: User? = null,
    val private: Boolean = false,
    val whitelisted: Set<User> = setOf(),
    val blacklisted: Set<User> = setOf(),
    val players: Set<User> = setOf(),
    val settings: WorldSettings = WorldSettings()
)
data class WorldSettings(
//    val generator: WorldGenerator,
    val damageMultiplier: Float = 1.0f,
    val pvp: Boolean = true,
    val netheriteAllowed: Boolean = true,
    val crystalsAllowed: Boolean = true,
    val anchorsAllowed: Boolean = true,
    val enderpearlsAllowed: Boolean = true,

)
// TODO: Add support for custom world generators for cpvp/uhc worlds
//data class WorldGenerator(
//    val type: WorldGenTypes,
//    val seed: Long
//)
//
//enum class WorldGenTypes {
//    FLAT,
//    NORMAL,
//    VOID
//}
//
//enum class FlatWorldGenSettings (
//    val layers: List<FlatWorldLayer>
//)
//
//data class FlatWorldLayer(
//    val material: Material,
//    val height: Int
//)

class WorldManager {
}