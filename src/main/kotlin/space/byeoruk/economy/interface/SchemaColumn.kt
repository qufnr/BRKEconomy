package space.byeoruk.economy.`interface`

interface SchemaColumn {
    fun column(): String
    fun type(): String
    fun columnName(): String
}