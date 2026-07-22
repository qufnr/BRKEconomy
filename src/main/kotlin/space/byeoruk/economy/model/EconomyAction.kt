package space.byeoruk.economy.model

import space.byeoruk.economy.`interface`.Describable

enum class EconomyAction : Describable {
    RECEIVE { override fun description(): String = "송금" },
    SET { override fun description(): String = "설정" },
    ADD { override fun description(): String = "추가" },
    WITHDRAW { override fun description(): String = "차감" },
    BUY { override fun description(): String = "구매" },
    SELL { override fun description(): String = "판매" },
    EVENT { override fun description(): String = "이벤트" },
    CUSTOM { override fun description(): String = "사용자 지정" }
}