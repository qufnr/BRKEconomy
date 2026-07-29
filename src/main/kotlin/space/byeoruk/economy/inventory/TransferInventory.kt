package space.byeoruk.economy.inventory

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import space.byeoruk.economy.MainPlugin
import space.byeoruk.lib.builder.item.ItemBuilder
import space.byeoruk.lib.item.IconItem
import space.byeoruk.lib.string.EmojiUtility.replaceEmojis
import space.byeoruk.lib.string.StringUtility.appendJosa
import java.math.BigDecimal
import java.util.UUID

class TransferInventory(val player: Player, val opponentUuid: UUID, val plugin: MainPlugin) {
    private val mm = MiniMessage.miniMessage()

    val inventory = MenuType.ANVIL
        .builder()
        .title(
            mm.deserialize(plugin.globalConfig.transferInventoryTitle)
                .replaceEmojis(plugin.globalConfig.namespace)
        )
        .checkReachable(false)
        .build(player)

    val opponent: Player? = Bukkit.getPlayer(opponentUuid)

    init {
        val currencyName = plugin.globalConfig.currencyName

        val invisibleIcon = try {
            ItemBuilder("invisible_icon", plugin.globalConfig.namespace)
                .build()
        } catch (e: RuntimeException) {
            ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                .build()
        }

        val firstSlotIcon = invisibleIcon.clone().apply {
            itemMeta = itemMeta?.apply {
                displayName(mm.deserialize("보낼 ${currencyName.appendJosa("을를")} 입력해 주세요"))
            }
        }

        val secondSlotIcon = invisibleIcon.clone().apply {
            itemMeta = itemMeta?.apply {
                displayName(mm.deserialize("<reset> "))
            }
        }

        inventory.setItem(0, firstSlotIcon)
        inventory.setItem(1, secondSlotIcon)
    }

    fun open() {
        inventory.open()
    }

    fun transfer(value: BigDecimal): Boolean {
        if (value <= BigDecimal.ZERO) {
            return false
        }

        val balance = plugin.economyManager.getBalance(player.uniqueId)
        if (balance < value) {
            return false
        }

        plugin.economyManager.withdrawBalance(player.uniqueId, balance)
        plugin.economyManager.depositBalance(player.uniqueId, balance)

        return true
    }

    fun getSubmitIcon(): ItemStack {
        val opponent = Bukkit.getPlayer(this.opponentUuid) ?: return ItemBuilder(Material.BARRIER)
            .displayName(mm.deserialize("<!italic><red>대상이 유효하지 않아요"))
            .lore(
                listOf(
                    mm.deserialize("<reset> "),
                    mm.deserialize("${IconItem.ACTION_PREFIX}클릭해서 닫아요")
                )
            )
            .build()

        val currencyName = plugin.globalConfig.currencyName
        val currencyUnitSymbol = plugin.globalConfig.currencyUnitSymbol
        val balance = plugin.economyManager.getBalance(player.uniqueId)
        val formattedBalance = plugin.economyManager.format(balance)

        return ItemBuilder(Material.GOLD_INGOT)
            .displayName(mm.deserialize("<!italic>${opponent.name}에게 $currencyName 보내기"))
            .lore(
                listOf(
                    mm.deserialize("${IconItem.LORE_PREFIX}보유 ${currencyName}: <color:#fdf55f>$formattedBalance</color> $currencyUnitSymbol")
                        .replaceEmojis(plugin.globalConfig.namespace),
                    mm.deserialize("<reset> "),
                    mm.deserialize("${IconItem.ACTION_PREFIX}클릭해서 ${currencyName.appendJosa("을를")} 보내요")
                )
            )
            .glowing(true)
            .build()
    }
}