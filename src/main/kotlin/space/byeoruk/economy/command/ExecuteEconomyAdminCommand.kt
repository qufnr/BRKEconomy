package space.byeoruk.economy.command

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import space.byeoruk.economy.MainPlugin
import space.byeoruk.lib.command.utility.ExecuteCommand
import java.math.BigDecimal
import kotlin.text.startsWith

interface ExecuteEconomyAdminCommand : ExecuteCommand {
    val tempNumbers: Set<Int>
        get() = setOf(1, 10, 50, 100, 500, 1000, 5000, 10000)

    val mm get() = MiniMessage.miniMessage()

    val mainPlugin: MainPlugin

    val config get() = mainPlugin.globalConfig
    val lang get() = config.lang

    override fun execute(sender: CommandSender, args: Array<String>) {
        val prefix = config.prefix

        if (args.size <= 1) {
            sender.sendMessage { mm.deserialize("$prefix${lang.getString("message.command.usage", "/${config.commandLabel} ${names.first()} <player> <amount>")}") }
            return
        }

        val opponent = getPlayerFromArgument(args[0], ignoreCase = true)
        if (opponent == null) {
            sender.sendMessage { mm.deserialize("$prefix${lang.getString("message.command.opponent-unknown")}") }
            return
        }

        val amount = args[1].toBigDecimalOrNull()
        if (amount == null) {
            sender.sendMessage { mm.deserialize("$prefix${lang.getString("message.invalid-number")}") }
            return
        }

        modifyBalance(sender, opponent, amount)
    }

    override fun suggest(sender: CommandSender, args: Array<String>): List<String> =
        when (args.size) {
            1 -> {
                mainPlugin.server.onlinePlayers
                    .map { it.name }
                    .filter { it.startsWith(args[0], true) }
            }

            2 -> {
                tempNumbers
                    .map { it.toString() }
                    .filter { it.startsWith(args[1]) }
            }

            else -> emptyList()
        }

    fun modifyBalance(sender: CommandSender, opponent: Player, amount: BigDecimal)
}