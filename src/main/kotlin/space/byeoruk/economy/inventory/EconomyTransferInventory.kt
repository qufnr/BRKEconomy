package space.byeoruk.economy.inventory

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import space.byeoruk.economy.MainPlugin
import space.byeoruk.economy.dto.TransferOpponent
import space.byeoruk.lib.inventory.utility.CustomAnvilInventory
import space.byeoruk.lib.item.builder.ItemBuilder
import space.byeoruk.lib.item.utility.ItemUtility
import space.byeoruk.lib.item.utility.ItemUtility.namespacedId
import space.byeoruk.lib.sound.utility.SoundUtility.playFailSound
import space.byeoruk.lib.sound.utility.SoundUtility.playOkSound
import space.byeoruk.lib.string.utility.EmojiUtility.replaceEmojis
import java.math.BigDecimal
import java.text.DecimalFormat

class EconomyTransferInventory(
    val plugin: MainPlugin,
    sender: Player,
    val opponent: TransferOpponent,
) : CustomAnvilInventory {
    override val viewer = sender

    private val mm = MiniMessage.miniMessage()

    private val config get() = plugin.globalConfig
    private val lang get() = config.lang
    private val economyManager get() = plugin.economyManager

    private val amount get() = renameText.toBigDecimalOrNull()

    override val view by lazy {
        createView(
            mm
                .deserialize(config.inventory.transferTitle)
                .replaceEmojis(config.namespace)
        )
    }

    init {
        inventory.setItem(0, buildEmptyIcon())
        inventory.setItem(1, buildTargetHeadIcon())
    }

    override fun onPrepareAnvil(event: PrepareAnvilEvent) {
        event.view.repairCost = 0
        event.view.maximumRepairCost = Int.MAX_VALUE
    }

    override fun onInventoryClick(event: InventoryClickEvent) {
        event.isCancelled = true

        if (event.rawSlot != 2 || !event.isLeftClick) {
            return
        }

        if (transfer()) {
            viewer.closeInventory()
        }
    }

    override fun onInventoryClose(event: InventoryCloseEvent) {
        economyManager.closeTransfer(viewer)
    }

    private fun transfer(): Boolean {
        //  한 번 더 검증한다
        val safeAmount = amount

        val prefix = config.prefix

        if (safeAmount == null) {
            viewer.sendActionBar { mm.deserialize("<red>${lang.getString("message.command.invalid-number")}</red>") }
            viewer.playFailSound()
            return false
        }

        if (safeAmount <= BigDecimal.ZERO) {
            viewer.sendActionBar { mm.deserialize("<red>${lang.getString("message.command.transfer.too-lower")}</red>") }
            viewer.playFailSound()
            return false
        }

        val balance = economyManager.getBalance(viewer.uniqueId)
        if (balance < safeAmount) {
            viewer.sendActionBar { mm.deserialize("<red>${lang.getString("message.command.transfer.shortage-balance")}</red>") }
            viewer.playFailSound()
            return false
        }

        economyManager.withdrawBalance(viewer.uniqueId, safeAmount)
        economyManager.depositBalance(opponent.uniqueId, safeAmount)

        val formatAmount = DecimalFormat(config.currencyFormat).format(safeAmount)

        viewer.sendMessage { mm.deserialize("$prefix${lang.getString("message.command.transfer.success", opponent.name, formatAmount)}") }

        val opponentPlayer = opponent.player ?: return true

        if (opponentPlayer.isOnline) {
            opponentPlayer.sendMessage { mm.deserialize("$prefix${lang.getString("message.command.transfer.success-to-target", viewer.name, formatAmount)}") }
            opponentPlayer.playOkSound()
        }

        return true
    }

    private fun buildEmptyIcon(): ItemStack =
        ItemBuilder(config.inventory.invisibleIcon, config.namespace)
            .displayName(lang.getString("message.command.transfer.enter-amount"))
            .build()

    private fun buildTargetHeadIcon(): ItemStack {
        val lorePrefix = ItemUtility.LORE_PREFIX
        val loreErrorPrefix = ItemUtility.LORE_ERROR_PREFIX
        val balance = economyManager.getBalance(opponent.uniqueId)
        val formatBalance = DecimalFormat(config.currencyFormat).format(balance)

        val displayName = "<!italic>${lang.getString("text.transfer")}"
        val lore = mutableListOf(
            "<reset> ",
            "$lorePrefix${lang.getString("text.transfer-target")}: <white>${opponent.name}</white>",
            "$lorePrefix${lang.getString("text.transfer-target-balance")}: <gold>${formatBalance} ${config.currencyName}</gold>"
        )

        val opponentPlayer = opponent.player
        return if (opponentPlayer?.isOnline == true) {
            ItemBuilder(Material.PLAYER_HEAD)
                .displayName(displayName)
                .lore(lore)
                .build()
        }
        else {
            ItemBuilder(Material.SKELETON_SKULL)
                .displayName(displayName)
                .lore(lore + "$loreErrorPrefix${lang.getString("message.command.transfer.opponent-is-offline")}")
                .build()
        }
    }
}