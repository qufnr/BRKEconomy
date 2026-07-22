package space.byeoruk.economy.manager

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import space.byeoruk.economy.MainPlugin
import space.byeoruk.economy.column.EconomyColumn
import space.byeoruk.economy.schema.EconomySchema
import java.io.File
import java.math.BigDecimal
import java.sql.Connection
import java.sql.SQLException
import java.util.UUID

class DatabaseManager(plugin: MainPlugin) {
    private val databaseConfig = plugin.globalConfig.database
    private val dataFolder = plugin.dataFolder

    private var dataSource: HikariDataSource? = null

    init {
        setupPool()
        createTable()
    }

    private fun setupPool() {
        val config = HikariConfig()
        
        if (databaseConfig.enabled) {
            config.jdbcUrl =
                "jdbc:mysql://${databaseConfig.url}/${databaseConfig.name}?useSSL=false&characterEncoding=UTF-8"
            config.driverClassName = databaseConfig.driverClassname
            config.username = databaseConfig.username
            config.password = databaseConfig.password

            //  MySQL 최적화 권장 설정
            config.addDataSourceProperty("cachePrepStmts", "true")
            config.addDataSourceProperty("prepStmtCacheSize", "250")
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            config.addDataSourceProperty("useServerPrepStmts", "true")
            config.addDataSourceProperty("useLocalSessionState", "true")
            config.addDataSourceProperty("rewriteBatchedStatements", "true")
            config.addDataSourceProperty("cacheResultSetMetadata", "true")
            config.addDataSourceProperty("cacheServerConfiguration", "true")
            config.addDataSourceProperty("elideSetAutoCommits", "true")
            config.addDataSourceProperty("maintainTimeStats", "false")

            //  Pool 사이즈 조절
            config.maximumPoolSize = databaseConfig.maximumPoolSize
            config.minimumIdle = databaseConfig.minimumIdle
            config.connectionTimeout = databaseConfig.connectionTimeout
            config.idleTimeout = databaseConfig.idleTimeout
        }
        else {
            val databaseFile = File(dataFolder, "brk_economy.db")
            if (!databaseFile.parentFile.exists()) {
                databaseFile.parentFile.mkdirs()
            }
            
            config.jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}"
            config.driverClassName = "org.sqlite.JDBC"
            
            //  SQLite-specific optimizations
            config.maximumPoolSize = 1
            config.connectionInitSql = "PRAGMA journal_mode=WAL; PRAGMA foreign_keys=ON;"
        }

        dataSource = HikariDataSource(config)
    }

    private fun createTable() {
        connection.use { conn ->
            conn.createStatement().execute(EconomySchema.createQuery())
        }
    }

    val connection: Connection
        get() = dataSource?.connection ?: throw SQLException("DataSource is not initialized")

    fun close() {
        dataSource?.close()
    }

    fun readBalance(uuid: UUID): BigDecimal {
        connection.use { conn ->
            conn.prepareStatement(EconomySchema.whereBalanceQuery())
                .use { preparedStatement ->
                    preparedStatement.setString(1, uuid.toString())
                    val result = preparedStatement.executeQuery()
                    if (result.next()) {
                        return result.getBigDecimal(EconomyColumn.BALANCE.column())
                    }
                }
        }

        return BigDecimal.ZERO
    }

    fun saveBalance(uuid: UUID, balance: BigDecimal) {
        connection.use { conn ->
            conn.prepareStatement(EconomySchema.updateQuery(!databaseConfig.enabled))
                .use { preparedStatement ->
                    preparedStatement.setString(1, uuid.toString())
                    preparedStatement.setBigDecimal(2, balance)
                    preparedStatement.executeUpdate()
                }
        }
    }

    fun hasAccount(uuid: UUID): Boolean {
        connection.use { conn ->
            conn.prepareStatement(EconomySchema.whereAccountQuery()).use { preparedStatement ->
                preparedStatement.setString(1, uuid.toString())
                val result = preparedStatement.executeQuery()
                return result.next()
            }
        }
    }
}