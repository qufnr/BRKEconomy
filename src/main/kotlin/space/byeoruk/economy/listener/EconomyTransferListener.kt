package space.byeoruk.economy.listener

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.AnvilView
import space.byeoruk.economy.MainPlugin
import space.byeoruk.economy.inventory.EconomyTransferInventory
import space.byeoruk.economy.utility.IconItemUtility
import space.byeoruk.lib.sound.SoundUtility.playFailSound
import space.byeoruk.lib.sound.SoundUtility.playOkSound
import space.byeoruk.lib.string.StringUtility.appendJosa
import java.math.BigDecimal

class EconomyTransferListener(
    private val plugin: MainPlugin
) : Listener {
    private val mm = MiniMessage.miniMessage()

    private fun isTransferMenu(player: Player, view: InventoryView): Boolean {
        return plugin.economyTransferManager.isTransfer(player) && view.type == InventoryType.ANVIL
    }

    /**
     * 모루 업데이트 이벤트
     *
     * @param event 이벤트
     */
    @EventHandler
    fun onPrepareAnvil(event: PrepareAnvilEvent) {
        val view = event.view
        val viewers = event.viewers

        for (viewer in viewers) {
            val player = viewer as Player
            if (!isTransferMenu(player, view)) {
                return
            }
        }

        view.repairCost = 0
        view.repairItemCountCost = 0
        view.maximumRepairCost = 0

        val player = event.view.player as Player
        val opponent = plugin.economyTransferManager.getTransferOpponent(player)

        event.result = IconItemUtility.transferIcon(plugin, player, opponent)
        //  이거 안하면 `result` 로 설정한 아이템 표시 안 됨
        player.updateInventory()
    }

    /**
     * 모루 창 닫을 때 이벤트
     *
     * 모루는 창을 닫으면 아이템이 떨어지기 때문에, 닫히기 전에 슬롯을 모두 비워줌
     *
     * @param event 이벤트
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryClose(event: InventoryCloseEvent) {
        val view = event.view
        val player = event.player as Player

        if (!isTransferMenu(player, view)) {
            return
        }

        val inventory = event.inventory
        for (i in 0..2) {
            inventory.setItem(i, ItemStack(Material.AIR))
        }
        plugin.economyTransferManager.clear(player)
    }

    /**
     * 모루 내 슬롯 클릭
     *
     * @param event 이벤트
     */
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val view = event.view
        val player = event.whoClicked as Player

        if (!isTransferMenu(player, view)) {
            return
        }

        val item = event.currentItem ?: return
        event.isCancelled = true
        val slot = event.rawSlot
        val anvilView = view as AnvilView

        if (slot == EconomyTransferInventory.CONFIRM_SLOT_INDEX) {
            if (item.type == Material.GOLD_INGOT) {
                val opponent = plugin.economyTransferManager.getTransferOpponent(player)
                if (opponent == null) {
                    player.sendActionBar { mm.deserialize("<red>대상이 유효하지 않아요") }
                    player.playFailSound()
                    player.closeInventory()
                    return
                }

                val inputBalance = anvilView.renameText?.toBigDecimalOrNull()
                if (inputBalance == null || inputBalance <= BigDecimal.ZERO) {
                    player.sendActionBar { mm.deserialize("<red>올바른 숫자를 입력해 주세요") }
                    player.playFailSound()
                    return
                }

                val currencyName = plugin.economyManager.currencyName()
                val balance = plugin.economyManager.getBalance(player.uniqueId)
                val formatInputBalance = plugin.economyManager.format(inputBalance)
                if (balance < inputBalance) {
                    player.sendActionBar { mm.deserialize("<red>$formatInputBalance 만큼 보낼 ${currencyName.appendJosa("이가")} 없어요") }
                    player.playFailSound()
                    return
                }

                val prefix = plugin.globalConfig.prefix
                if (!plugin.economyManager.transfer(player.uniqueId, opponent.uniqueId, inputBalance)) {
                    player.sendMessage { mm.deserialize("${prefix}${currencyName.appendJosa("을를")} 보내는 데 실패했어요") }
                    player.playFailSound()
                }
                else {
                    player.sendMessage { mm.deserialize("${prefix}${opponent.name}에게 ${currencyName.appendJosa("을를")} <white>${formatInputBalance}</white> 만큼 보냈어요") }
                    player.playOkSound()
                    opponent.sendMessage { mm.deserialize("${prefix}${player.name}이(가) 나에게 ${currencyName.appendJosa("을를")} <white>${formatInputBalance}</white> 만큼 보냈어요") }
                    opponent.playOkSound()
                }
            }
            player.closeInventory()
        }
    }
}