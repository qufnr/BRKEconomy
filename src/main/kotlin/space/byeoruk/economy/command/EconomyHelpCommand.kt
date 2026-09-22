package space.byeoruk.economy.command

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import space.byeoruk.economy.MainPlugin
import space.byeoruk.lib.command.model.CommandAccessType
import space.byeoruk.lib.command.utility.ExecuteCommand

class EconomyHelpCommand(val plugin: MainPlugin, private val commands: () -> List<ExecuteCommand>) : ExecuteCommand {
    override val names = listOf("도움말", "help")
    override val description = "message.command.help.description"
    override val permission = EconomyPermission.HELP
    override val accessTypes = setOf(CommandAccessType.PLAYER, CommandAccessType.CONSOLE)

    private val mm = MiniMessage.miniMessage()

    private val config get() = plugin.globalConfig
    private val lang get() = config.lang

    override fun execute(sender: CommandSender, args: Array<String>) {
        sender.sendMessage { mm.deserialize("<reset> ") }
        sender.sendMessage { mm.deserialize("${config.prefix}Economy Commands:") }
        sender.sendMessage { mm.deserialize("${config.prefix}/${config.commandLabel} <white>- ${lang.getString("message.command.main.description")}") }

        commands()
            .filter { it.canAccess(sender) }
            .forEach {
                sender.sendMessage { mm.deserialize("${config.prefix}/${config.commandLabel} ${it.names.first()} <white>- ${lang.getString(it.description)}") }
            }

        sender.sendMessage { mm.deserialize("<reset> ") }
    }
}