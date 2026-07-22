package space.byeoruk.economy.manager

import org.bukkit.entity.Player

class EconomyTransferManager {
    private val transfers = mutableMapOf<Player, Player>()

    fun isTransfer(player: Player): Boolean = transfers.contains(player)

    fun setTransferOpponent(player: Player, opponent: Player) {
        transfers[player] = opponent
    }

    fun getTransferOpponent(player: Player): Player? {
        val opponent = transfers[player] ?: return null

        if (!opponent.isOnline) {
            transfers.remove(player)
            return null
        }

        return opponent
    }

    fun clear(player: Player) {
        transfers.remove(player)
    }
}