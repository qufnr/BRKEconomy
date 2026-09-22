package space.byeoruk.economy.dto

import org.bukkit.entity.Player
import space.byeoruk.economy.MainPlugin
import java.util.UUID

data class TransferOpponent(
    val plugin: MainPlugin,
    val name: String,
    val uniqueId: UUID
) {
    val player: Player? get() = plugin.server.getPlayer(uniqueId)
}
