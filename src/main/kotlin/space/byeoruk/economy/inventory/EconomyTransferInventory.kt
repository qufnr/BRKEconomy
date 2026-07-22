package space.byeoruk.economy.inventory

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import space.byeoruk.economy.MainPlugin
import space.byeoruk.lib.builder.item.ItemBuilder
import space.byeoruk.lib.string.EmojiUtility.replaceEmojis
import space.byeoruk.lib.string.StringUtility.appendJosa

object EconomyTransferInventory {
    private val mm = MiniMessage.miniMessage()

    const val AMOUNT_SLOT_INDEX = 0
    const val EMPTY_SLOT_INDEX = 1
    const val CONFIRM_SLOT_INDEX = 2

    fun inventoryTitle(plugin: MainPlugin): Component = Component.text(plugin.globalConfig.transferInventoryTitle)
        .replaceEmojis(plugin.globalConfig.namespace)

    fun open(plugin: MainPlugin, player: Player, opponent: Player) {
        plugin.economyTransferManager.setTransferOpponent(player, opponent)

        val currencyName = plugin.globalConfig.currencyName

        val inventory = MenuType.ANVIL
            .builder()
            .title(inventoryTitle(plugin))
            .checkReachable(false)
            .build(player)

        val icon = try {
            ItemBuilder("invisible_icon", plugin.globalConfig.namespace)
                .build()
        }
        catch(e: RuntimeException) {
            ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                .build()
        }

        val amountIcon = icon.clone().apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text("보낼 ${currencyName.appendJosa("을를")} 입력해 주세요"))
            }
        }

        val emptySlotIcon = icon.clone().apply {
            itemMeta = itemMeta?.apply {
                displayName(mm.deserialize("<reset> "))
            }
        }

        inventory.setItem(AMOUNT_SLOT_INDEX, amountIcon)
        inventory.setItem(EMPTY_SLOT_INDEX, emptySlotIcon)

        inventory.open()
    }
}