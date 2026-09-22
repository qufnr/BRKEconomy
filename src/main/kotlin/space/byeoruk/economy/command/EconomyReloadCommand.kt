package space.byeoruk.economy.command

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import space.byeoruk.economy.MainPlugin
import space.byeoruk.lib.command.model.CommandAccessType
import space.byeoruk.lib.command.utility.ExecuteCommand

class EconomyReloadCommand(val plugin: MainPlugin) : ExecuteCommand {
    override val names = listOf("리로드", "reload")
    override val description = "message.command.reload.description"
    override val permission = EconomyPermission.RELOAD
    override val accessTypes = setOf(CommandAccessType.PLAYER, CommandAccessType.CONSOLE)

    private val mm = MiniMessage.miniMessage()

    private val config get() = plugin.globalConfig
    private val economyManager get() = plugin.economyManager

    override fun execute(sender: CommandSender, args: Array<String>) {
        plugin.reloadConfigs()
        economyManager.closeTransferAll().forEach {
            it.sendMessage { mm.deserialize("${config.prefix}${config.lang.getString("message.command.reload.transfer-safe-closed")}") }
        }

        if (sender is Player) {
            sender.sendMessage { mm.deserialize("${config.prefix}${config.lang.getString("message.command.reload.success")}") }
        }
        else {
            plugin.logger.info(config.lang.getString("message.command.reload.success"))
        }
    }
}