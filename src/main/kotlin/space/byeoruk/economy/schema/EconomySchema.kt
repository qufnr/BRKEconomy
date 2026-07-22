package space.byeoruk.economy.schema

import space.byeoruk.economy.column.EconomyColumn

object EconomySchema {
    fun createQuery(): String {
        val columns = EconomyColumn.entries.joinToString(", ") { "${it.column()} ${it.type()}" }
        return "CREATE TABLE IF NOT EXISTS economy($columns);"
    }

    fun whereBalanceQuery(): String =
        """
            SELECT ${EconomyColumn.BALANCE.column()}
            FROM economy
            WHERE ${EconomyColumn.PLAYER_UUID.column()} = ?
        """.trimIndent()

    fun updateQuery(sqlite: Boolean = false): String {
        var query = """
            INSERT INTO economy (${EconomyColumn.PLAYER_UUID.column()}, ${EconomyColumn.BALANCE.column()})
            VALUES(?, ?)
        """.trimIndent()

        query +=
            if (sqlite)
                " ON CONFLICT(${EconomyColumn.PLAYER_UUID.column()}) DO UPDATE SET ${EconomyColumn.BALANCE.column()} = excluded.${EconomyColumn.BALANCE.column()}"
            else
                " ON DUPLICATE KEY UPDATE ${EconomyColumn.BALANCE.column()} = VALUES(${EconomyColumn.BALANCE.column()})"

        return query
    }

    fun whereAccountQuery(): String =
        """
            SELECT 1 
            FROM economy 
            WHERE ${EconomyColumn.PLAYER_UUID.column()} = ?
        """.trimIndent()
}