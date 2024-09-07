package me.pog5.andromeda.managers

data class Punishment(
    val player: User,
    val reason: String,
    val duration: Long,
    val staff: User?,
    val date: Long,
    val active: Boolean,
)

class PunishmentManager {
}