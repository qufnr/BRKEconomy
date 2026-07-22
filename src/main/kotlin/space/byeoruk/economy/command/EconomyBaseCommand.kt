package space.byeoruk.economy.command

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import space.byeoruk.economy.MainPlugin
import space.byeoruk.economy.inventory.EconomyTransferInventory
import space.byeoruk.lib.command.CommandUtility
import space.byeoruk.lib.string.EmojiUtility.replaceEmojis
import space.byeoruk.lib.string.StringUtility.appendJosa

class EconomyBaseCommand(
    private val plugin: MainPlugin,
) : BasicCommand {
    private val mm = MiniMessage.miniMessage()

    private val commandLabels = listOf("도움말", "보내기", "보기")
    private val commandOpLabels = listOf("설정", "추가", "차감")

    override fun suggest(commandSourceStack: CommandSourceStack, args: Array<out String>): Collection<String> {
        val sender = commandSourceStack.sender as? Player ?: return emptyList()
        return when (args.size) {
            0 -> {
                if (sender.isOp)
                    commandLabels + commandOpLabels
                else
                    commandLabels
            }

            //  /레이블 [여기]
            1 -> {
                val input = args[0]

                val labels =
                    if (sender.isOp)
                        commandLabels + commandOpLabels
                    else
                        commandLabels

                labels.filter { it.startsWith(input, true) }
            }

            //  /레이블 [] [여기]
            2 -> {
                val argument = args[0].lowercase()
                val input = args[1]

                when (argument) {
                    commandLabels[1], commandLabels[2] -> {
                        return Bukkit.getOnlinePlayers()
                            .map { it.name }
                            .filter { if (input.isBlank()) true else it.lowercase().startsWith(input) }
                    }
                    in commandOpLabels -> {
                        if (sender.isOp) {
                            return Bukkit.getOnlinePlayers()
                                .map { it.name }
                                .filter { if (input.isBlank()) true else it.lowercase().startsWith(input) }
                        }
                    }
                }

                emptyList()
            }

            3 -> {
                if (sender.isOp) {
                    val argument = args[0].lowercase()
                    val input = args[2]

                    when (argument) {
                        in commandOpLabels -> {
                            return listOf(10, 50, 100, 500, 1000)
                                .map { it.toString() }
                                .filter { if (input.isBlank()) true else it.startsWith(input) }
                        }
                    }
                }

                emptyList()
            }

            else -> emptyList()
        }
    }

    override fun execute(commandSourceStack: CommandSourceStack, args: Array<out String>) {
        val sender = commandSourceStack.sender as? Player ?: return

        val prefix = plugin.globalConfig.prefix
        val currencyName = plugin.globalConfig.currencyName
        val currencyUnitSymbol = plugin.globalConfig.currencyUnitSymbol

        if (args.isEmpty()) {
            val balance = plugin.economyManager.getBalance(sender.uniqueId)
            val decimalValue = plugin.economyManager.format(balance)
            sender.sendMessage { mm.deserialize("${prefix}보유 $currencyName: <color:#fdf55f>$decimalValue</color> $currencyUnitSymbol")
                .replaceEmojis(plugin.globalConfig.namespace)
            }
            return
        }

        when (args[0].lowercase()) {
            "도움말" -> executeHelpCommand(sender)
            "보기" -> executeDetailCommand(sender, args)
            "보내기" -> executeTransferCommand(sender, args)
            "설정", "추가", "차감" -> executeOpCommand(sender, args[0], args)
        }
    }

    private fun executeHelpCommand(sender: Player) {
        val label = plugin.globalConfig.commandLabel
        val prefix = plugin.globalConfig.prefix
        val currencyName = plugin.globalConfig.currencyName

        sender.sendMessage { mm.deserialize("<reset> ") }
        sender.sendMessage { mm.deserialize("${prefix}BRKEconomy 명령어 도움말:") }
        sender.sendMessage { mm.deserialize("${prefix}/$label 도움말 - <white>도움말 메시지를 출력해요") }
        sender.sendMessage { mm.deserialize("${prefix}/$label [보기] [플레이어] - <white>내 ${currencyName.appendJosa("을를")} 확인하거나, 대상의 ${currencyName.appendJosa("을를")} 확인해요") }
        sender.sendMessage { mm.deserialize("${prefix}/$label 보내기 <플레이어> - <white>대상에게 ${currencyName.appendJosa("을를")} 보내요") }

        if (sender.isOp) {
            sender.sendMessage { mm.deserialize("${prefix}/$label 설정 <플레이어> <수치> - <white>대상의 ${currencyName.appendJosa("을를")} 수치만큼 설정해요 <red>[관리자 명령어]") }
            sender.sendMessage { mm.deserialize("${prefix}/$label 추가 <플레이어> <수치> - <white>대상의 ${currencyName.appendJosa("을를")} 수치만큼 추가해요 <red>[관리자 명령어]") }
            sender.sendMessage { mm.deserialize("${prefix}/$label 차감 <플레이어> <수치> - <white>대상의 ${currencyName}에 수치만큼 차감해요 <red>[관리자 명령어]") }
        }

        sender.sendMessage { mm.deserialize("<reset> ") }
    }

    private fun executeDetailCommand(sender: Player, args: Array<out String>) {
        val label = plugin.globalConfig.commandLabel
        val prefix = plugin.globalConfig.prefix

        if (args.size == 1 && args[0].equals(commandLabels[2], true)) {
            sender.sendMessage { mm.deserialize("${prefix}사용법: /$label 보기 <플레이어>") }
            return
        }

        if (args.size == 2) {
            val opponent = CommandUtility.findOpponent(sender, args[1], true, prefix) ?: return

            val currencyName = plugin.globalConfig.currencyName
            val currencyUnitSymbol = plugin.globalConfig.currencyUnitSymbol
            val balance = plugin.economyManager.getBalance(opponent.uniqueId)
            val decimalValue = plugin.economyManager.format(balance)

            if (sender == opponent) {
                sender.sendMessage { mm.deserialize("${prefix}보유 $currencyName: <color:#fdf55f>$decimalValue</color> $currencyUnitSymbol")
                    .replaceEmojis(plugin.globalConfig.namespace)
                }
            }
            else {
                sender.sendMessage { mm.deserialize("${prefix}${opponent.name}의 $currencyName: <color:#fdf55f>$decimalValue</color> $currencyUnitSymbol")
                    .replaceEmojis(plugin.globalConfig.namespace)
                }
            }
        }
    }

    private fun executeTransferCommand(sender: Player, args: Array<out String>) {
        val label = plugin.globalConfig.commandLabel
        val prefix = plugin.globalConfig.prefix

        if (args.size == 1) {
            sender.sendMessage { mm.deserialize("${prefix}사용법: /$label 보내기 <플레이어>") }
            return
        }

        val opponent = CommandUtility.findOpponent(sender, args[1], false, prefix) ?: return

        EconomyTransferInventory.open(plugin, sender, opponent)
    }

    private fun executeOpCommand(sender: Player, command: String, args: Array<out String>) {
        if (!sender.isOp) {
            return
        }

        val label = plugin.globalConfig.commandLabel
        val prefix = plugin.globalConfig.prefix

        if (args.size == 1) {
            sender.sendMessage { mm.deserialize("${prefix}사용법: /$label ${args[0]} <플레이어> <수치>") }
            return
        }

        if (args.size >= 2) {
            val opponent = CommandUtility.findOpponent(sender, args[1], true, prefix) ?: return
            val value = if (args.size > 2) CommandUtility.getPositiveNumber(sender, args[2], prefix) ?: return
            else return

            val amount = value.toBigDecimal()

            val currencyName = plugin.globalConfig.currencyName
            val formattedAmount = plugin.economyManager.format(amount)

            when (command) {
                commandOpLabels[0] -> {
                    sender.sendMessage { mm.deserialize("${prefix}${opponent.name}의 ${currencyName.appendJosa("을를")} <white>${formattedAmount}</white> (으)로 설정했어요") }
                    plugin.economyManager.setBalance(opponent.uniqueId, amount)
                    opponent.sendMessage { mm.deserialize("${prefix}관리자에 의해 나의 ${currencyName.appendJosa("이가")} <white>${formattedAmount}</white> (으)로 설정됐어요") }
                }
                commandOpLabels[1] -> {
                    sender.sendMessage { mm.deserialize("${prefix}${opponent.name}의 ${currencyName.appendJosa("을를")} <white>${formattedAmount}</white> 만큼 추가했어요") }
                    plugin.economyManager.depositBalance(opponent.uniqueId, amount)
                    opponent.sendMessage { mm.deserialize("${prefix}관리자에 의해 나의 ${currencyName.appendJosa("이가")} <white>${formattedAmount}</white> 만큼 추가됐어요") }
                }
                commandOpLabels[2] -> {
                    sender.sendMessage { mm.deserialize("${prefix}${opponent.name}의 ${currencyName.appendJosa("을를")} <white>${formattedAmount}</white> 만큼 차감했어요") }
                    plugin.economyManager.withdrawBalance(opponent.uniqueId, amount)
                    opponent.sendMessage { mm.deserialize("${prefix}관리자에 의해 나의 ${currencyName.appendJosa("이가")} <white>${formattedAmount}</white> 만큼 차감됐어요") }
                }
            }

            return
        }
    }
}