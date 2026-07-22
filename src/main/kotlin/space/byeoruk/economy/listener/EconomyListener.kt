package space.byeoruk.economy.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import space.byeoruk.economy.MainPlugin

class EconomyListener(private val plugin: MainPlugin) : Listener {
    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        plugin.economyTransferManager.clear(player)
        plugin.economyManager.readBalance(player.uniqueId)
    }

    @EventHandler
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        plugin.economyManager.saveBalance(player.uniqueId)
    }
}