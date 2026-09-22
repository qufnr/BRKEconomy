package space.byeoruk.economy.command

object EconomyPermission {
    const val HELP = "economy.help"
    const val INFO = "economy.info"
    const val TRANSFER = "economy.transfer"
    const val SET = "economy.set"
    const val ADD = "economy.add"
    const val SUBTRACT = "economy.subract"
    const val RELOAD = "economy.reload"

    val ALL = listOf(HELP, INFO, TRANSFER, SET, ADD, SUBTRACT, RELOAD)
}