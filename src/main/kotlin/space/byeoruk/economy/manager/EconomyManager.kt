package space.byeoruk.economy.manager

import space.byeoruk.economy.MainPlugin
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.UUID

class EconomyManager(private val plugin: MainPlugin) {
    private val balances = mutableMapOf<UUID, BigDecimal>()

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
     */
    fun setBalance(uuid: UUID, value: BigDecimal) {
        balances[uuid] = value
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
            val targetBalance = plugin.databaseManager.readBalance(uuid)
            plugin.databaseManager.saveBalance(uuid, targetBalance + value)
            return
        }

        balances[uuid] = balances[uuid]!! + value
    }

    /**
     * 자금 차감
     *
     * @param uuid 플레이어 UUID
     * @param value 추가할 자금
     */
    fun withdrawBalance(uuid: UUID, value: BigDecimal) {
        if (value <= BigDecimal.ZERO) {
            return
        }

        if (!balances.containsKey(uuid)) {
            val targetBalance = plugin.databaseManager.readBalance(uuid)
            //  차감했을 때 음수일 경우 0으로 설정
            if (targetBalance - value < BigDecimal.ZERO) {
                plugin.databaseManager.saveBalance(uuid, BigDecimal.ZERO)
            }
            else {
                plugin.databaseManager.saveBalance(uuid, targetBalance - value)
            }
            return
        }

        //  위 조건과 마찬가지 음수 방지
        if (balances[uuid]!! - value < BigDecimal.ZERO) {
            balances[uuid] = BigDecimal.ZERO
        }
        else {
            balances[uuid] = balances[uuid]!! - value
        }
    }

    /**
     * 자금 읽기
     *
     * @param uuid 플레이어 UUID
     */
    fun readBalance(uuid: UUID) {
        val balance = plugin.databaseManager.readBalance(uuid)
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

        plugin.databaseManager.saveBalance(uuid, balance)
    }

    /**
     * 자금 보내기
     *
     * @param uuid 플레이어 UUID
     * @param opponent 대상 UUID
     * @param value 보낼 자금
     * @return 자금을 보냈을 경우 true 아니면 false 반환
     */
    fun transfer(uuid: UUID, opponent: UUID, value: BigDecimal): Boolean {
        if (value <= BigDecimal.ZERO) {
            return false
        }

        val senderBalance = balances[uuid] ?: plugin.databaseManager.readBalance(uuid)
        if (senderBalance < value) {
            return false
        }

        withdrawBalance(uuid, value)
        depositBalance(opponent, value)

        return true
    }
}