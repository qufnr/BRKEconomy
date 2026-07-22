package space.byeoruk.economy.vault

import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.OfflinePlayer
import space.byeoruk.economy.MainPlugin
import java.math.BigDecimal

@Suppress("DEPRECATION")
class BRKLegacyEconomy(
    private val plugin: MainPlugin,
    private val economy: BRKEconomy
) : Economy {
    override fun isEnabled(): Boolean = economy.isEnabled

    override fun getName(): String = economy.name

    override fun hasBankSupport(): Boolean = false

    override fun fractionalDigits(): Int = economy.fractionalDigits(plugin.name)

    override fun format(amount: Double): String = economy.format(plugin.name, BigDecimal.valueOf(amount))

    override fun currencyNamePlural(): String = plugin.globalConfig.currencyName

    override fun currencyNameSingular(): String = plugin.globalConfig.currencyName

    override fun hasAccount(player: OfflinePlayer): Boolean = economy.hasAccount(player.uniqueId)

    @Deprecated("Deprecated in Java")
    override fun hasAccount(playerName: String): Boolean {
        val player = plugin.server.getOfflinePlayer(playerName)
        return hasAccount(player)
    }

    override fun hasAccount(player: OfflinePlayer, worldName: String?): Boolean = hasAccount(player)

    @Deprecated("Deprecated in Java")
    override fun hasAccount(playerName: String, worldName: String?): Boolean = hasAccount(playerName)

    override fun getBalance(player: OfflinePlayer): Double =
        economy.balance(plugin.name, player.uniqueId).toDouble()

    @Deprecated("Deprecated in Java")
    override fun getBalance(playerName: String): Double =
        getBalance(plugin.server.getOfflinePlayer(playerName))

    override fun getBalance(player: OfflinePlayer, world: String?): Double = getBalance(player)

    @Deprecated("Deprecated in Java")
    override fun getBalance(playerName: String, world: String?): Double = getBalance(playerName)

    override fun has(player: OfflinePlayer, amount: Double): Boolean =
        economy.has(plugin.name, player.uniqueId, BigDecimal.valueOf(amount))

    @Deprecated("Deprecated in Java")
    override fun has(playerName: String, amount: Double): Boolean =
        has(plugin.server.getOfflinePlayer(playerName), amount)

    override fun has(player: OfflinePlayer, worldName: String?, amount: Double): Boolean = has(player, amount)

    @Deprecated("Deprecated in Java")
    override fun has(playerName: String, worldName: String?, amount: Double): Boolean = has(playerName, amount)

    override fun withdrawPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        val res = economy.withdraw(plugin.name, player.uniqueId, BigDecimal.valueOf(amount))
        val type =
            if (res.transactionSuccess()) EconomyResponse.ResponseType.SUCCESS else EconomyResponse.ResponseType.FAILURE
        return EconomyResponse(res.amount.toDouble(), res.balance.toDouble(), type, res.errorMessage)
    }

    @Deprecated("Deprecated in Java")
    override fun withdrawPlayer(playerName: String, amount: Double): EconomyResponse =
        withdrawPlayer(plugin.server.getOfflinePlayer(playerName), amount)

    override fun withdrawPlayer(player: OfflinePlayer, worldName: String?, amount: Double): EconomyResponse =
        withdrawPlayer(player, amount)

    @Deprecated("Deprecated in Java")
    override fun withdrawPlayer(playerName: String, worldName: String?, amount: Double): EconomyResponse =
        withdrawPlayer(playerName, amount)

    override fun depositPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        val res = economy.deposit(plugin.name, player.uniqueId, BigDecimal.valueOf(amount))
        val type =
            if (res.transactionSuccess()) EconomyResponse.ResponseType.SUCCESS else EconomyResponse.ResponseType.FAILURE
        return EconomyResponse(res.amount.toDouble(), res.balance.toDouble(), type, res.errorMessage)
    }

    @Deprecated("Deprecated in Java")
    override fun depositPlayer(playerName: String, amount: Double): EconomyResponse =
        depositPlayer(plugin.server.getOfflinePlayer(playerName), amount)

    override fun depositPlayer(player: OfflinePlayer, worldName: String?, amount: Double): EconomyResponse =
        depositPlayer(player, amount)

    @Deprecated("Deprecated in Java")
    override fun depositPlayer(playerName: String, worldName: String?, amount: Double): EconomyResponse =
        depositPlayer(playerName, amount)

    override fun createPlayerAccount(player: OfflinePlayer): Boolean =
        economy.createAccount(player.uniqueId, player.name ?: player.uniqueId.toString())

    @Deprecated("Deprecated in Java")
    override fun createPlayerAccount(playerName: String): Boolean =
        createPlayerAccount(plugin.server.getOfflinePlayer(playerName))

    override fun createPlayerAccount(player: OfflinePlayer, worldName: String?): Boolean = createPlayerAccount(player)

    @Deprecated("Deprecated in Java")
    override fun createPlayerAccount(playerName: String, worldName: String?): Boolean = createPlayerAccount(playerName)

    override fun createBank(name: String?, player: OfflinePlayer?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported")

    @Deprecated("Deprecated in Java")
    override fun createBank(name: String?, playerName: String?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported")

    override fun deleteBank(name: String?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported")

    override fun bankBalance(name: String?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported")

    override fun bankHas(name: String?, amount: Double): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported")

    override fun bankWithdraw(name: String?, amount: Double): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported")

    override fun bankDeposit(name: String?, amount: Double): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported")

    override fun isBankOwner(name: String?, player: OfflinePlayer?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported")

    @Deprecated("Deprecated in Java")
    override fun isBankOwner(name: String?, playerName: String?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported")

    override fun isBankMember(name: String?, player: OfflinePlayer?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported")

    @Deprecated("Deprecated in Java")
    override fun isBankMember(name: String?, playerName: String?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported")

    override fun getBanks(): MutableList<String> = mutableListOf()
}