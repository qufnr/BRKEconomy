package space.byeoruk.economy.listener

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.view.AnvilView
import space.byeoruk.economy.MainPlugin
import space.byeoruk.lib.sound.SoundUtility.playFailSound
import space.byeoruk.lib.sound.SoundUtility.playOkSound
import space.byeoruk.lib.string.StringUtility.appendJosa
import java.math.BigDecimal

class EconomyTransferListener(
    private val plugin: MainPlugin
) : Listener {
    private val mm = MiniMessage.miniMessage()

    private fun isTransferring(player: Player, view: InventoryView): Boolean {
        val transfer = plugin.economyManager.getTransferInventory(player) ?: return false

        return plugin.economyManager.isTransfer(player) &&
                transfer.inventory.type == InventoryType.ANVIL &&
                transfer.inventory.type == view.type
    }

    /**
     * 모루 업데이트 이벤트
     *
     * @param event 이벤트
     */
    @EventHandler
    fun onPrepareAnvil(event: PrepareAnvilEvent) {
        val view = event.view
        val player = event.view.player as? Player ?: return

        if (!isTransferring(player, view)) {
            player.closeInventory()
            return
        }

        //  무조건 있어야 함
        val transferInventory = plugin.economyManager.getTransferInventory(player)!!

        view.repairCost = 0
        view.repairItemCountCost = 0
        view.maximumRepairCost = 0

        event.result = transferInventory.getSubmitIcon()
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

        if (!isTransferring(player, view)) {
            return
        }

        for (i in 0..2) {
            event.inventory.setItem(i, null)
        }

        plugin.economyManager.closeTransfer(player)
    }

    /**
     * 모루 내 슬롯 클릭
     *
     * @param event 이벤트
     */
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        if (!isTransferring(player, event.view)) {
            return
        }

        event.isCancelled = true

        val slot = event.rawSlot
        val view = event.view as AnvilView

        //  결과 아이템 슬롯이 아니면 클릭 무시
        if (slot != 2) {
            return
        }

        val transferInventory = plugin.economyManager.getTransferInventory(player)!!

        val opponent = transferInventory.opponent
        if (opponent == null) {
            player.playFailSound()
            player.closeInventory()
            return
        }

        val inputValue = view.renameText?.toBigDecimalOrNull()
        if (inputValue == null || inputValue <= BigDecimal.ZERO) {
            player.sendActionBar { mm.deserialize("<red>올바른 숫자를 입력해 주세요") }
            player.playFailSound()
            return
        }

        val currencyName = plugin.economyManager.currencyName()
        val balance = plugin.economyManager.getBalance(player.uniqueId)
        val formatInputBalance = plugin.economyManager.format(inputValue)
        if (balance < inputValue) {
            player.sendActionBar { mm.deserialize("<red>${currencyName.appendJosa("이가")} 부족해요") }
            player.playFailSound()
            return
        }

        val prefix = plugin.globalConfig.prefix
        if (!transferInventory.transfer(inputValue)) {
            player.sendMessage { mm.deserialize("${prefix}${currencyName.appendJosa("을를")} 보내는 데 실패했어요") }
            player.playFailSound()
        }
        else {
            player.sendMessage { mm.deserialize("${prefix}${opponent.name}에게 ${currencyName.appendJosa("을를")} <white>${formatInputBalance}</white> 만큼 보냈어요") }
            player.playOkSound()
            opponent.sendMessage { mm.deserialize("${prefix}${player.name}이(가) 나에게 ${currencyName.appendJosa("을를")} <white>${formatInputBalance}</white> 만큼 보냈어요") }
            opponent.playOkSound()
        }
        player.closeInventory()
    }
}