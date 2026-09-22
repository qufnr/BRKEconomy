package space.byeoruk.economy.config

import space.byeoruk.economy.MainPlugin
import space.byeoruk.lib.config.utility.LangConfiguration
import space.byeoruk.lib.database.dto.DatabaseConfiguration

data class GlobalConfig(
    val lang: LangConfiguration,
    val langCode: String = "ko",
    val database: DatabaseConfiguration,
    val inventory: InventoryConfig,
    val prefix: String = "<grey>[@] ",
    val namespace: String = "byeoruksmp",
    val commandLabel: String = "골드",
    val currencyName: String = "골드",
    val currencyUnitSymbol: String = ":gold:",
    val currencyFormat: String = "#,###",
) {
    companion object {
        fun build(plugin: MainPlugin): GlobalConfig {
            plugin.saveDefaultConfig()
            plugin.saveConfig()

            val config = plugin.config
            val langCode = config.getString("economy.lang", "ko") ?: "ko"

            return GlobalConfig(
                lang = LangConfiguration.load(plugin, langCode),
                database = DatabaseConfiguration.build(plugin, "economy.database"),
                inventory = InventoryConfig.build(config),
                prefix = config.getString("economy.prefix", "<grey>[@] ") ?: "<grey>[@] ",
                namespace = config.getString("economy.namespace", "byeoruksmp") ?: "byeoruksmp",
                commandLabel = config.getString("economy.command-label", "골드") ?: "골드",
                currencyName = config.getString("economy.currency-name", "골드") ?: "골드",
                currencyUnitSymbol = config.getString("economy.currency-unit-symbol", ":gold:") ?: ":gold:",
                currencyFormat = config.getString("economy.currency-format", "#,###") ?: "#,###",
            )
        }
    }
}