package me.pog5.andromeda.managers

data class Punishment(
    val player: User,
    val reason: String,
    val duration: Long,
    val staff: User?,
    val timestamp: Long,
) {
    val expired: Boolean
        get() = System.currentTimeMillis() > timestamp + duration
    val id: Int
        get() = this.hashCode()
}

class PunishmentManager