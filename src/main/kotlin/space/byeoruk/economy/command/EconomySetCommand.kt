package space.byeoruk.economy.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import space.byeoruk.economy.MainPlugin
import space.byeoruk.lib.command.model.CommandAccessType
import java.math.BigDecimal
import java.text.DecimalFormat

class EconomySetCommand(val plugin: MainPlugin) : ExecuteEconomyAdminCommand {
    override val names = listOf("설정", "set")
    override val description = "command"
    override val permission = EconomyPermission.SET
    override val accessTypes = setOf(CommandAccessType.CONSOLE, CommandAccessType.PLAYER)

    override val mainPlugin = plugin
    private val economyManager get() = plugin.economyManager

    override fun modifyBalance(sender: CommandSender, opponent: Player, amount: BigDecimal) {
        val prefix = config.prefix
        val formatAmount = DecimalFormat(config.currencyFormat).format(amount)
        sender.sendMessage { mm.deserialize("$prefix${lang.getString("message.command.set.success", opponent.name, formatAmount)}") }
        if (opponent.isOnline) {
            opponent.sendMessage { mm.deserialize("$prefix${lang.getString("message.command.set.success-to-target", formatAmount)}") }
        }
        economyManager.setBalance(opponent.uniqueId, amount)
    }
}