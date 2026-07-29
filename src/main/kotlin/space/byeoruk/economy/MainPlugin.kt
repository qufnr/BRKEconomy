package space.byeoruk.economy

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.milkbowl.vault2.economy.Economy
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import space.byeoruk.economy.command.EconomyBaseCommand
import space.byeoruk.economy.config.GlobalConfig
import space.byeoruk.economy.listener.EconomyListener
import space.byeoruk.economy.listener.EconomyTransferListener
import space.byeoruk.economy.manager.EconomyManager
import space.byeoruk.economy.manager.DatabaseManager
import space.byeoruk.economy.vault.BRKEconomy
import space.byeoruk.economy.vault.BRKLegacyEconomy

class MainPlugin : JavaPlugin() {
    lateinit var globalConfig: GlobalConfig
        private set
    lateinit var economyManager: EconomyManager
        private set
    lateinit var databaseManager: DatabaseManager
        private set

    lateinit var economy: Economy
        private set

    override fun onEnable() {
        if (!setupEconomy()) {
            logger.severe("[%s] Disabled due to no Vault dependency found".format(name))
            server.pluginManager.disablePlugin(this)
            return
        }

        globalConfig = GlobalConfig(this)

        registerManagers()
        registerEventListeners()
        registerCommands()
    }

    override fun onDisable() {
        //  서버에 남아있는 플레이어 데이터 저장 처리
        //  서버가 종료되는 시점에는 플러그인 비활성화 후 PlayerQuitEvent 가 터지기 때문에, 여기서 저장 처리해야 함
        if (::economyManager.isInitialized) {
            server.onlinePlayers.forEach { economyManager.saveBalance(it.uniqueId) }
        }

        //  데이터베이스 커넥션 풀 닫기
        if (::databaseManager.isInitialized) {
            databaseManager.close()
        }
    }

    private fun registerManagers() {
        databaseManager = DatabaseManager(this)
        economyManager = EconomyManager(this)
    }

    private fun registerEventListeners() {
        server.pluginManager.registerEvents(EconomyListener(this), this)
        server.pluginManager.registerEvents(EconomyTransferListener(this), this)
    }

    private fun registerCommands() {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(globalConfig.commandLabel, EconomyBaseCommand(this))
        }
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }

        val brkEconomy = BRKEconomy(this)
        //  Vault2
        server.servicesManager.register(Economy::class.java, brkEconomy, this, ServicePriority.Normal)
        //  Legacy Vault
        server.servicesManager.register(net.milkbowl.vault.economy.Economy::class.java, BRKLegacyEconomy(this, brkEconomy), this, ServicePriority.Normal)
        economy = brkEconomy

        return true
    }
}