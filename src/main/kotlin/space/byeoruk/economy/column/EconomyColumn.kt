package space.byeoruk.economy.column

import space.byeoruk.economy.`interface`.SchemaColumn

enum class EconomyColumn : SchemaColumn {
    PLAYER_UUID {
        override fun column(): String = "player_uuid"
        override fun type(): String = "VARCHAR(36) PRIMARY KEY"
        override fun columnName(): String = "플레이어 UUID"
    },

    BALANCE {
        override fun column(): String = "balance"
        override fun type(): String = "DECIMAL(38, 18) NOT NULL DEFAULT 0"
        override fun columnName(): String = "자금"
    }
}