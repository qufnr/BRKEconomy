package space.byeoruk.economy.command

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import space.byeoruk.economy.MainPlugin
import space.byeoruk.lib.command.model.CommandAccessType
import space.byeoruk.lib.command.utility.ExecuteCommand

class EconomyInfoCommand(val plugin: MainPlugin) : ExecuteCommand {
    override val names = listOf("정보", "info")
    override val description = "message.command.info.description"
    override val permission = EconomyPermission.INFO
    override val accessTypes = setOf(CommandAccessType.PLAYER, CommandAccessType.CONSOLE)

    private val mm = MiniMessage.miniMessage()

    private val config get() = plugin.globalConfig
    private val lang get() = config.lang
    private val economyManager get() = plugin.economyManager

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            if (sender !is Player) {
                sender.sendMessage { mm.deserialize("${config.prefix}${lang.getString("message.command.usage", "/${config.commandLabel} ${names.first()} <player>")}") }
                return
            }

            val balance = economyManager.getBalance(sender.uniqueId)
            val formatBalance = "%,d %s".format(balance.toLong(), config.currencyName)

            sender.sendMessage { mm.deserialize("${config.prefix}${lang.getString("text.info", formatBalance)}") }
        }

        val name = args[0]
        val opponent = plugin.server.onlinePlayers.first { it.name.equals(name, true) }

        if (opponent == null || !opponent.isOnline) {
            sender.sendMessage { mm.deserialize("${config.prefix}${lang.getString("message.command.opponent-unknown")}") }
            return
        }

        val balance = economyManager.getBalance(opponent.uniqueId)
        val formatBalance = "%,d %s".format(balance.toLong(), config.currencyName)

        sender.sendMessage { mm.deserialize("${config.prefix}${lang.getString("text.opponent-info", opponent.name, formatBalance)}") }
    }

    override fun suggest(sender: CommandSender, args: Array<String>): List<String> =
        if (args.size <= 1)
            plugin.server.onlinePlayers
                .map { it.name }
                .filter { it.startsWith(args[0], true) }
        else
            emptyList()
}