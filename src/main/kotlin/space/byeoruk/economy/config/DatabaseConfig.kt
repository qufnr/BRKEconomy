package space.byeoruk.economy.config

import org.bukkit.configuration.file.FileConfiguration

class DatabaseConfig(config: FileConfiguration) {
    var enabled: Boolean = false
        private set
    var url: String = "localhost:3306"
        private set
    var name: String = "brk_smp"
        private set
    var driverClassname: String = "com.mysql.cj.jdbc.Driver"
        private set
    var username: String = "root"
        private set
    var password: String = "1234"
        private set
    var maximumPoolSize: Int = 10
        private set
    var minimumIdle: Int = 5
        private set
    var connectionTimeout: Long = 10000
        private set
    var idleTimeout: Long = 600000
        private set

    init {
        enabled = config.getBoolean("database.enabled", false)
        url = config.getString("database.url", "localhost:3306") ?: "localhost:3306"
        name = config.getString("database.name", "brk_smp") ?: "brk_smp"
        driverClassname = config.getString("database.driver-class-name", "com.mysql.cj.jdbc.Driver") ?: "com.mysql.cj.jdbc.Driver"
        username = config.getString("database.username", "root") ?: "root"
        password = config.getString("database.password", "1234") ?: "1234"
        maximumPoolSize = config.getInt("database.maximum-pool-size", 10)
        minimumIdle = config.getInt("database.minimum-idle", 5)
        connectionTimeout = config.getLong("database.connection-timeout", 10000)
        idleTimeout = config.getLong("database.idle-timeout", 600000)
    }
}