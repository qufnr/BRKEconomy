package space.byeoruk.economy.command

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import space.byeoruk.economy.MainPlugin
import space.byeoruk.economy.dto.TransferOpponent
import space.byeoruk.lib.command.model.CommandAccessType
import space.byeoruk.lib.command.utility.ExecuteCommand

class EconomyTransferCommand(val plugin: MainPlugin) : ExecuteCommand {
    override val names = listOf("보내기", "transfer")
    override val description = "message.command.transfer.description"
    override val permission = EconomyPermission.TRANSFER
    override val accessTypes = setOf(CommandAccessType.PLAYER)

    private val mm = MiniMessage.miniMessage()

    private val config get() = plugin.globalConfig
    private val lang get() = config.lang
    private val economyManager get() = plugin.economyManager

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (sender !is Player) {
            return
        }

        val prefix = config.prefix

        if (args.isEmpty()) {
            sender.sendMessage { mm.deserialize("$prefix${lang.getString("message.command.usage", "/${config.commandLabel} ${names.first()} <player>")}") }
            return
        }

        //  대상
        val opponent = getPlayerFromArgument(args[0], ignoreCase = true)
        var transferOpponent: TransferOpponent
        if (opponent != null) {
            transferOpponent = TransferOpponent(plugin = plugin, name = opponent.name, uniqueId = opponent.uniqueId)
        }
        else {
            val offlineOpponent = getOfflinePlayerFromArgument(args[0], ignoreCase = true)
            if (offlineOpponent != null) {
                transferOpponent = TransferOpponent(plugin = plugin, name = offlineOpponent.name ?: "", offlineOpponent.uniqueId)
            }
            else {
                sender.sendMessage { mm.deserialize("$prefix${lang.getString("message.command.opponent-unknown")}") }
                return
            }
        }

        economyManager.openTransfer(sender, transferOpponent)
    }

    override fun suggest(sender: CommandSender, args: Array<String>): List<String> =
        if (args.size <= 1)
            plugin.server.onlinePlayers
                .map { it.name }
                .filter { it.startsWith(args[0], true) }
        else
            emptyList()
}