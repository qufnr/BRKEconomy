package space.byeoruk.economy.config

import space.byeoruk.economy.MainPlugin

class GlobalConfig(plugin: MainPlugin) {
    var prefix: String = "<grey>[@] "
        private set
    var transferInventoryTitle: String = ":offset_-60::economy_transfer:"
        private set
    var commandLabel: String = "골드"
        private set
    var currencyName: String = "골드"
        private set
    var currencyUnitSymbol: String = ":gold:"
        private set
    var currencyFormat: String = "#,###"
        private set
    var database: DatabaseConfig
        private set
    var namespace: String = "byeoruksmp"
        private set

    init {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()

        val config = plugin.config

        prefix = config.getString("text.prefix", "<grey>[@] ") ?: "<grey>[@] "
        transferInventoryTitle = config.getString("text.transfer-inventory-title", ":offset_-60::economy_transfer:") ?: ":offset_-60::economy_transfer:"
        commandLabel = config.getString("command-label", "골드") ?: "골드"
        currencyName = config.getString("currency.name", "골드") ?: "골드"
        currencyUnitSymbol = config.getString("currency.unit-name", ":gold:") ?: ":gold:"
        currencyFormat = config.getString("currency.format", "#,###") ?: "#,###"
        namespace = config.getString("namespace", "byeoruksmp") ?: "byeoruksmp"

        database = DatabaseConfig(config)
    }
}