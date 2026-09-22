package space.byeoruk.economy.command

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import space.byeoruk.economy.MainPlugin
import space.byeoruk.lib.command.utility.ExecuteCommand

class EconomyCommand(val plugin: MainPlugin) : BasicCommand {
    private val mm = MiniMessage.miniMessage()

    private val config get() = plugin.globalConfig
    private val lang get() = config.lang
    private val economyManager get() = plugin.economyManager

    private val subCommands = mutableListOf<ExecuteCommand>()

    init {
        subCommands += EconomyHelpCommand(plugin) { subCommands }
        subCommands += EconomyInfoCommand(plugin)
        subCommands += EconomyTransferCommand(plugin)
        subCommands += EconomySetCommand(plugin)
        subCommands += EconomySubtractCommand(plugin)
        subCommands += EconomyAddCommand(plugin)
        subCommands += EconomyReloadCommand(plugin)
    }

    override fun execute(source: CommandSourceStack, args: Array<String>) {
        val sender = source.sender

        //  인자가 없으면 내 자금 보여주기
        if (args.isEmpty()) {
            showMyInfo(sender)
            return
        }

        val subCommand = subCommands.firstOrNull { args[0].lowercase() in it.names }
        if (subCommand == null) {
            sender.sendMessage { mm.deserialize("${config.prefix}${lang.getString("message.command.unknown")}") }
            return
        }

        if (!subCommand.canAccess(sender)) {
            sender.sendMessage { mm.deserialize("${config.prefix}${lang.getString("message.command.no-permission")}") }
            return
        }

        subCommand.execute(sender, args.drop(1).toTypedArray())
    }

    override fun suggest(source: CommandSourceStack, args: Array<String>): Collection<String> {
        val sender = source.sender

        if (args.size <= 1) {
            val typed = args.firstOrNull().orEmpty()

            return subCommands
                .filter { it.canAccess(sender) }
                .flatMap { it.names }
                .filter { it.startsWith(typed, true) }
        }

        val subCommand = subCommands.firstOrNull { args[0].lowercase() in it.names } ?: return emptyList()
        if (!subCommand.canAccess(sender)) {
            return emptyList()
        }

        return subCommand.suggest(sender, args.drop(1).toTypedArray())
    }

    override fun canUse(sender: CommandSender): Boolean = EconomyPermission.ALL.any { sender.hasPermission(it) }

    private fun showMyInfo(sender: CommandSender) {
        if (sender !is Player) {
            return
        }

        val balance = economyManager.getBalance(sender.uniqueId)
        val formatBalance = "%,d %s".format(balance.toLong(), config.currencyName)

        sender.sendMessage { mm.deserialize("${config.prefix}${lang.getString("text.info", formatBalance)}") }
    }
}