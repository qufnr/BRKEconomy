package space.byeoruk.economy.utility

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import space.byeoruk.economy.MainPlugin
import space.byeoruk.lib.builder.item.ItemBuilder
import space.byeoruk.lib.item.IconItem
import space.byeoruk.lib.string.EmojiUtility.replaceEmojis
import space.byeoruk.lib.string.StringUtility.appendJosa

object IconItemUtility {
    private val mm = MiniMessage.miniMessage()

    fun transferIcon(pluginInstance: MainPlugin, player: Player, opponent: Player?): ItemStack {
        val currencyName = pluginInstance.globalConfig.currencyName
        val currencyUnitSymbol = pluginInstance.globalConfig.currencyUnitSymbol
        val balance = pluginInstance.economyManager.getBalance(player.uniqueId)
        val formattedBalance = pluginInstance.economyManager.format(balance)

        return if (opponent == null)
            ItemBuilder(Material.BARRIER)
                .displayName(mm.deserialize("<!italic><red>대상이 유효하지 않아요"))
                .lore(listOf(
                    mm.deserialize("<reset> "),
                    mm.deserialize("${IconItem.ACTION_PREFIX}클릭해서 닫아요")
                ))
                .build()
        else
            ItemBuilder(Material.GOLD_INGOT)
                .displayName(mm.deserialize("<!italic>${opponent.name}에게 $currencyName 보내기"))
                .lore(listOf(
                    mm.deserialize("${IconItem.LORE_PREFIX}보유 ${currencyName}: <color:#fdf55f>$formattedBalance</color> $currencyUnitSymbol")
                        .replaceEmojis(pluginInstance.globalConfig.namespace),
                    mm.deserialize("<reset> "),
                    mm.deserialize("${IconItem.ACTION_PREFIX}클릭해서 ${currencyName.appendJosa("을를")} 보내요")
                ))
                .glowing(true)
                .build()
    }
}