package space.byeoruk.economy.column

import space.byeoruk.economy.`interface`.SchemaColumn

enum class EconomyLogColumn : SchemaColumn {
    UID {
        override fun column(): String = "uid"
        override fun type(): String = "INT NOT NULL AUTO_INCREMENT PRIMARY KEY"
        override fun columnName(): String = "로그 ID"
    },

    ACTION_TYPE {
        override fun column(): String = "action_type"
        override fun type(): String = "VARCHAR(32) NOT NULL"
        override fun columnName(): String = "로그 유형"
    },

    ACTOR_UUID {
        override fun column(): String = "actor_uuid"
        override fun type(): String = "VARCHAR(36)"
        override fun columnName(): String = "거래 발생 주체 UUID (보내는 사람, 구매자, 또는 관리자)"
    },

    ACTOR_NAME {
        override fun column(): String = "actor_name"
        override fun type(): String = "VARCHAR(16)"
        override fun columnName(): String = "거래 발생 주체 이름"
    },

    OPPONENT_UUID {
        override fun column(): String = "opponent_uuid"
        override fun type(): String = "VARCHAR(36)"
        override fun columnName(): String = "거래 대상 UUID (받는 사람)"
    },

    OPPONENT_NAME {
        override fun column(): String = "opponent_name"
        override fun type(): String = "VARCHAR(16)"
        override fun columnName(): String = "거래 대상 이름"
    },

    AMOUNT {
        override fun column(): String = "amount"
        override fun type(): String = "DECIMAL(38, 18) NOT NULL"
        override fun columnName(): String = "거래 금액"
    },

    ACTOR_BEFORE_BALANCE {
        override fun column(): String = "actor_before_balance"
        override fun type(): String = "DECIMAL(38, 18)"
        override fun columnName(): String = "주체의 거래 이전 잔액 (관리자나 시스템의 경우 NULL)"
    },

    ACTOR_AFTER_BALANCE {
        override fun column(): String = "actor_after_balance"
        override fun type(): String = "DECIMAL(38, 18)"
        override fun columnName(): String = "주체의 거래 이후 잔액"
    },

    OPPONENT_BEFORE_BALANCE {
        override fun column(): String = "opponent_before_balance"
        override fun type(): String = "DECIMAL(38, 18)"
        override fun columnName(): String = "대상의 거래 이전 잔액"
    },

    OPPONENT_AFTER_BALANCE {
        override fun column(): String = "opponent_after_balance"
        override fun type(): String = "DECIMAL(38, 18)"
        override fun columnName(): String = "대상의 거래 이후 잔액"
    },

    REASON {
        override fun column(): String = "reason"
        override fun type(): String = "VARCHAR(255)"
        override fun columnName(): String = "거래 사유"
    },

    CREATED_AT {
        override fun column(): String = "created_at"
        override fun type(): String = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
        override fun columnName(): String = "거래 일자"
    }
}