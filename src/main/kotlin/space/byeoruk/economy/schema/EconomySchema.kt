package space.byeoruk.economy.schema

import space.byeoruk.economy.column.EconomyColumn
import space.byeoruk.lib.database.model.SchemaQuery

object EconomySchema : SchemaQuery<EconomyColumn> {
    override val entries = EconomyColumn.entries
    override val table = "economy"

    override fun createQuery(useSqlite: Boolean): String {
        val columns = columnsWithType(useSqlite)
        return if (!useSqlite)
            "$createTableQuery($columns) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;"
        else
            "$createTableQuery($columns);"
    }

    fun whereBalanceQuery(): String =
        """
            SELECT ${EconomyColumn.BALANCE.column()}
            FROM $table 
            WHERE ${EconomyColumn.PLAYER_UUID.column()} = ?
        """.trimIndent()

    fun updateQuery(useSqlite: Boolean): String {
        val playerUuid = EconomyColumn.PLAYER_UUID.column()
        val balance = EconomyColumn.BALANCE.column()

        var query = """
            INSERT INTO $table($playerUuid, $balance) 
            VALUES(?, ?)
        """.trimIndent()

        query +=
            if (useSqlite)
                " ON CONFLICT($playerUuid) DO UPDATE SET $balance = excluded.$balance"
            else
                " ON DUPLICATE KEY UPDATE $balance = VALUES($balance)"

        return query
    }

    fun whereAccountQuery(): String =
        """
            SELECT 1 
            FROM $table 
            WHERE ${EconomyColumn.PLAYER_UUID.column()} = ?
        """.trimIndent()
}