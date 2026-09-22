package space.byeoruk.economy.manager

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import space.byeoruk.economy.MainPlugin
import space.byeoruk.economy.column.EconomyColumn
import space.byeoruk.economy.schema.EconomySchema
import space.byeoruk.lib.database.manager.DatabaseManager
import space.byeoruk.lib.database.utility.DatabaseUtility
import java.io.File
import java.math.BigDecimal
import java.util.UUID
import javax.sql.DataSource

class EconomyDatabaseManager(val plugin: MainPlugin) : DatabaseManager {
    private val dataFolder = plugin.dataFolder

    private val databaseConfig get() = plugin.globalConfig.database
    private val config get() = plugin.globalConfig
    private val useSqlite get() = !config.database.enabled

    override var dataSource: DataSource? = null
        private set

    override val executor = DatabaseUtility.singleThread("${plugin.name}-Database")

    override val logger get() = plugin.logger

    init {
        setupPool()
        createTable()
    }

    private fun setupPool() {
        val hikariConfig = HikariConfig()
        
        if (!useSqlite) {
            hikariConfig.jdbcUrl =
                "jdbc:mysql://${databaseConfig.url}/${databaseConfig.name}?useSSL=false&characterEncoding=UTF-8"
            hikariConfig.driverClassName = databaseConfig.driverClassname
            hikariConfig.username = databaseConfig.username
            hikariConfig.password = databaseConfig.password

            //  MySQL 최적화 권장 설정
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250")
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true")
            hikariConfig.addDataSourceProperty("useLocalSessionState", "true")
            hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true")
            hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true")
            hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true")
            hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true")
            hikariConfig.addDataSourceProperty("maintainTimeStats", "false")

            //  Pool 사이즈 조절
            hikariConfig.maximumPoolSize = databaseConfig.maximumPoolSize
            hikariConfig.minimumIdle = databaseConfig.minimumIdle
            hikariConfig.connectionTimeout = databaseConfig.connectionTimeout
            hikariConfig.idleTimeout = databaseConfig.idleTimeout
        }
        else {
            val databaseFile = File(dataFolder, "brk_economy.db")
            if (!databaseFile.parentFile.exists()) {
                databaseFile.parentFile.mkdirs()
            }
            
            hikariConfig.jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}"
            hikariConfig.driverClassName = "org.sqlite.JDBC"
            
            //  SQLite-specific optimizations
            hikariConfig.maximumPoolSize = 1
            hikariConfig.connectionInitSql = "PRAGMA journal_mode=WAL; PRAGMA foreign_keys=ON;"
        }

        dataSource = HikariDataSource(hikariConfig)
    }

    private fun createTable() {
        newConnection().use { conn ->
            conn.createStatement().execute(EconomySchema.createQuery(useSqlite))
        }
    }

    fun close() = closeDatabase(10L)

    fun readBalance(uuid: UUID): BigDecimal {
        newConnection().use { conn ->
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
        newConnection().use { conn ->
            conn.prepareStatement(EconomySchema.updateQuery(!databaseConfig.enabled))
                .use { preparedStatement ->
                    preparedStatement.setString(1, uuid.toString())
                    preparedStatement.setBigDecimal(2, balance)
                    preparedStatement.executeUpdate()
                }
        }
    }

    fun hasAccount(uuid: UUID): Boolean {
        newConnection().use { conn ->
            conn.prepareStatement(EconomySchema.whereAccountQuery()).use { preparedStatement ->
                preparedStatement.setString(1, uuid.toString())
                val result = preparedStatement.executeQuery()
                return result.next()
            }
        }
    }
}