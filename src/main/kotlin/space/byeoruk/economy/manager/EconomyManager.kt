package space.byeoruk.economy.manager

import org.bukkit.entity.Player
import space.byeoruk.economy.MainPlugin
import space.byeoruk.economy.dto.TransferOpponent
import space.byeoruk.economy.inventory.EconomyTransferInventory
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class EconomyManager(private val plugin: MainPlugin) {
    private val balances = mutableMapOf<UUID, BigDecimal>()
    private val transfers = ConcurrentHashMap<UUID, EconomyTransferInventory>()

    /**
     * 자금 포맷
     *
     * @param balance 자금
     * @return 포맷된 자금 문자열
     */
    fun format(balance: BigDecimal): String =
        DecimalFormat(plugin.globalConfig.currencyFormat).format(balance)

    /**
     * 화폐 단위
     *
     * @return 화폐 단위 문자열
     */
    fun currencyName(): String = plugin.globalConfig.currencyName

    /**
     * 자금 반환
     *
     * @param uuid 플레이어 UUID
     * @return 자금
     */
    fun getBalance(uuid: UUID): BigDecimal = balances[uuid] ?: BigDecimal.ZERO

    /**
     * 자금 설정
     *
     * @param uuid 플레이어 UUID
     * @param value 설정할 자금
     */
    fun setBalance(uuid: UUID, value: BigDecimal) {
        if (!balances.containsKey(uuid)) {
            plugin.economyDatabaseManager.saveBalance(uuid, value)
            return
        }

        balances[uuid] = value
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            plugin.economyDatabaseManager.saveBalance(uuid, value)
        })
    }

    /**
     * 자금 추가
     *
     * @param uuid 플레이어 UUID
     * @param value 추가할 자금
     */
    fun depositBalance(uuid: UUID, value: BigDecimal) {
        if (value <= BigDecimal.ZERO) {
            return
        }

        if (!balances.containsKey(uuid)) {
            val targetBalance = plugin.economyDatabaseManager.readBalance(uuid)
            plugin.economyDatabaseManager.saveBalance(uuid, targetBalance + value)
            return
        }

        val newBalance = balances[uuid]!! + value
        balances[uuid] = newBalance
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            plugin.economyDatabaseManager.saveBalance(uuid, newBalance)
        })
    }

    /**
     * 자금 차감
     *
     * @param uuid 플레이어 UUID
     * @param value 차감할 자금
     */
    fun withdrawBalance(uuid: UUID, value: BigDecimal) {
        if (value <= BigDecimal.ZERO) {
            return
        }

        if (!balances.containsKey(uuid)) {
            val targetBalance = plugin.economyDatabaseManager.readBalance(uuid)
            // 차감했을 때 음수일 경우 0으로 설정
            val newBalance = if (targetBalance - value < BigDecimal.ZERO) BigDecimal.ZERO else targetBalance - value
            plugin.economyDatabaseManager.saveBalance(uuid, newBalance)
            return
        }

        // 위 조건과 마찬가지 음수 방지
        val currentBalance = balances[uuid]!!
        val newBalance = if (currentBalance - value < BigDecimal.ZERO) BigDecimal.ZERO else currentBalance - value
        balances[uuid] = newBalance

        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            plugin.economyDatabaseManager.saveBalance(uuid, newBalance)
        })
    }

    /**
     * 자금 읽기
     *
     * @param uuid 플레이어 UUID
     */
    fun readBalance(uuid: UUID) {
        val balance = plugin.economyDatabaseManager.readBalance(uuid)
        balances[uuid] = balance
    }

    /**
     * 자금 저장
     *
     * @param uuid 플레이어 UUID
     */
    fun saveBalance(uuid: UUID) {
        val balance = balances[uuid]
        if (balance == null) {
            plugin.logger.info { "플레이어($uuid)의 자금을 저장하는 데 실패했어요: 저장할 자금이 존재하지 않아요" }
            return
        }

        plugin.economyDatabaseManager.saveBalance(uuid, balance)
    }

    /**
     * 자금 맵 비우기
     *
     * @param uuid 플레이어 UUID
     */
    fun removeBalance(uuid: UUID) {
        balances.remove(uuid)
    }

    /**
     * 자금 전송 열기
     *
     * @param player 플레이어
     * @param opponent 대상 플레이어 DTO
     */
    fun openTransfer(player: Player, opponent: TransferOpponent) {
        val transfer = transfers.getOrPut(player.uniqueId) {
            EconomyTransferInventory(plugin, player, opponent)
        }
        transfer.open()
    }

    /**
     * 자금 전송 닫기
     *
     * @param player 플레이어
     */
    fun closeTransfer(player: Player) {
        transfers.remove(player.uniqueId)
    }

    /**
     * 자금 전송 중인 플레이어 화면 닫기
     *
     * @return 플레이어 목록
     */
    fun closeTransferAll(): List<Player> {
        val closedViewers = mutableListOf<Player>()
        transfers.values.forEach { transfer ->
            transfer.viewer.closeInventory()
            closedViewers += transfer.viewer
        }

        return closedViewers
    }
}