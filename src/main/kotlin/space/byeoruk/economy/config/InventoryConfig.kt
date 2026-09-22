package space.byeoruk.economy.config

import org.bukkit.configuration.file.FileConfiguration

data class InventoryConfig(
    val transferTitle: String = ":offset_-60::economy_transfer:",
    val transferConfirmIcon: String = "minecraft:gold",
    val invisibleIcon: String = "invisible_icon",
) {
    companion object {
        fun build(config: FileConfiguration): InventoryConfig = InventoryConfig(
            transferTitle = config.getString("economy.inventory.transfer.title", ":offset_-60::economy_transfer:") ?: ":offset_-60::economy_transfer:",
            transferConfirmIcon = config.getString("economy.inventory.transfer.confirm-icon", "minecraft:gold_ingot") ?: "minecraft:gold_ingot",
            invisibleIcon = config.getString("economy.inventory.invisible-icon", "invisible_icon") ?: "invisible_icon"
        )
    }
}
