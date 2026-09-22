package space.byeoruk.economy.vault

import net.milkbowl.vault2.economy.AccountPermission
import net.milkbowl.vault2.economy.Economy
import net.milkbowl.vault2.economy.EconomyResponse
import space.byeoruk.economy.MainPlugin
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Optional
import java.util.UUID

class BRKEconomy(
    private val plugin: MainPlugin
) : Economy {
    override fun isEnabled(): Boolean = true

    override fun getName(): String = "BRKEconomy"

    override fun hasSharedAccountSupport(): Boolean = false

    override fun hasMultiCurrencySupport(): Boolean = false

    override fun fractionalDigits(pluginName: String): Int = 0

    override fun format(pluginName: String, amount: BigDecimal): String =
        DecimalFormat(plugin.globalConfig.currencyFormat)
            .format(amount)

    @Deprecated("Deprecated in Java")
    override fun format(amount: BigDecimal): String = format(plugin.name, amount)

    @Deprecated("Deprecated in Java")
    override fun format(amount: BigDecimal, currency: String): String = format(plugin.name, amount, currency)

    override fun format(pluginName: String, amount: BigDecimal, currency: String): String {
        val formatAmount = format(pluginName, amount)
        return formatAmount
    }

    override fun hasCurrency(currency: String): Boolean = currency == plugin.globalConfig.currencyName

    override fun getDefaultCurrency(pluginName: String): String = plugin.globalConfig.currencyName

    override fun defaultCurrencyNamePlural(pluginName: String): String = plugin.globalConfig.currencyName

    override fun defaultCurrencyNameSingular(pluginName: String): String = plugin.globalConfig.currencyName

    override fun currencies(): Collection<String?> = listOf(plugin.globalConfig.currencyName)

    @Deprecated("Deprecated in Java")
    override fun createAccount(accountId: UUID, name: String): Boolean {
        if (!hasAccount(accountId)) {
            plugin.economyDatabaseManager.saveBalance(accountId, BigDecimal.ZERO)
        }
        return true
    }

    override fun createAccount(accountId: UUID, name: String, player: Boolean): Boolean =
        createAccount(accountId, name)

    @Deprecated("Deprecated in Java")
    override fun createAccount(accountId: UUID, name: String, worldName: String): Boolean =
        createAccount(accountId, name)

    override fun createAccount(accountId: UUID, name: String, worldName: String, player: Boolean): Boolean =
        createAccount(accountId, name)

    override fun getUUIDNameMap(): Map<UUID?, String?> = emptyMap()

    @Suppress("UNCHECKED_CAST")
    override fun getAccountName(accountId: UUID): Optional<String?>? =
        Optional.ofNullable(plugin.server.getOfflinePlayer(accountId).name) as Optional<String?>?

    override fun hasAccount(accountId: UUID): Boolean {
        val player = plugin.server.getPlayer(accountId)
        if (player != null && player.isOnline) {
            return true
        }
        return plugin.economyDatabaseManager.hasAccount(accountId)
    }

    override fun hasAccount(accountId: UUID, worldName: String): Boolean =
        hasAccount(accountId)

    override fun renameAccount(accountId: UUID, name: String): Boolean = true

    override fun renameAccount(plugin: String, accountId: UUID, name: String): Boolean = true

    override fun deleteAccount(plugin: String, accountId: UUID): Boolean = true

    override fun accountSupportsCurrency(plugin: String, accountId: UUID, currency: String): Boolean = true

    override fun accountSupportsCurrency(plugin: String, accountId: UUID, currency: String, world: String): Boolean = true

    @Deprecated("Deprecated in Java")
    override fun getBalance(pluginName: String, accountId: UUID): BigDecimal = balance(pluginName, accountId)

    @Deprecated("Deprecated in Java")
    override fun getBalance(pluginName: String, accountId: UUID, world: String): BigDecimal = balance(pluginName, accountId, world)

    @Deprecated("Deprecated in Java")
    override fun getBalance(pluginName: String, accountId: UUID, world: String, currency: String): BigDecimal = balance(pluginName, accountId, world, currency)

    override fun balance(pluginName: String, accountId: UUID): BigDecimal {
        val player = plugin.server.getPlayer(accountId)
        return if (player != null && player.isOnline) {
            plugin.economyManager.getBalance(accountId)
        } else {
            plugin.economyDatabaseManager.readBalance(accountId)
        }
    }

    override fun balance(pluginName: String, accountId: UUID, world: String): BigDecimal = balance(pluginName, accountId)

    override fun balance(pluginName: String, accountId: UUID, world: String, currency: String): BigDecimal = balance(pluginName, accountId)

    override fun has(pluginName: String, accountId: UUID, amount: BigDecimal): Boolean = balance(pluginName, accountId) >= amount

    override fun has(pluginName: String, accountId: UUID, worldName: String, amount: BigDecimal): Boolean = balance(pluginName, accountId, worldName) >= amount

    override fun has(pluginName: String, accountId: UUID, worldName: String, currency: String, amount: BigDecimal): Boolean = balance(pluginName, accountId, worldName, currency) >= amount

    /**
     * 자금 차감
     *
     * @param pluginName 플러그인 이름
     * @param accountId 계좌 UUID
     * @param amount 차감할 자금
     * @return 경제 응답
     */
    override fun withdraw(pluginName: String, accountId: UUID, amount: BigDecimal): EconomyResponse {
        if (amount < BigDecimal.ZERO) {
            return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amount")
        }
        val currentBalance = balance(pluginName, accountId)
        if (currentBalance < amount) {
            return EconomyResponse(BigDecimal.ZERO, currentBalance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds")
        }
        val newBalance = currentBalance - amount

        plugin.economyManager.setBalance(accountId, newBalance)

        return EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "")
    }

    override fun withdraw(pluginName: String, accountId: UUID, worldName: String, amount: BigDecimal): EconomyResponse = withdraw(pluginName, accountId, amount)

    override fun withdraw(pluginName: String, accountId: UUID, worldName: String, currency: String, amount: BigDecimal): EconomyResponse = withdraw(pluginName, accountId, amount)

    override fun deposit(pluginName: String, accountId: UUID, amount: BigDecimal): EconomyResponse {
        if (amount < BigDecimal.ZERO) {
            return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amount")
        }
        val currentBalance = balance(pluginName, accountId)
        val newBalance = currentBalance + amount

        plugin.economyManager.setBalance(accountId, newBalance)

        return EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "")
    }

    override fun deposit(pluginName: String, accountId: UUID, worldName: String, amount: BigDecimal): EconomyResponse = deposit(pluginName, accountId, amount)

    override fun deposit(pluginName: String, accountId: UUID, worldName: String, currency: String, amount: BigDecimal): EconomyResponse = deposit(pluginName, accountId, amount)

    override fun createSharedAccount(pluginName: String, accountId: UUID, name: String, owner: UUID): Boolean = false

    override fun isAccountOwner(pluginName: String, accountId: UUID, uuid: UUID): Boolean = false

    override fun setOwner(pluginName: String, accountId: UUID, uuid: UUID): Boolean = false

    override fun isAccountMember(pluginName: String, accountId: UUID, uuid: UUID): Boolean = false

    override fun addAccountMember(pluginName: String, accountId: UUID, uuid: UUID): Boolean = false

    override fun addAccountMember(pluginName: String, accountId: UUID, uuid: UUID, vararg initialPermissions: AccountPermission): Boolean = false

    override fun removeAccountMember(pluginName: String, accountId: UUID, uuid: UUID): Boolean = false

    override fun hasAccountPermission(pluginName: String, accountId: UUID, uuid: UUID, permission: AccountPermission): Boolean = false

    override fun updateAccountPermission(pluginName: String, accountId: UUID, uuid: UUID, permission: AccountPermission, value: Boolean): Boolean = false
}