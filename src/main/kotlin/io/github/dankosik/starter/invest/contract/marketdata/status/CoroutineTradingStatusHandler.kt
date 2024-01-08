package io.github.dankosik.starter.invest.contract.marketdata.status

import ru.tinkoff.piapi.contract.v1.TradingStatus

interface CoroutineTradingStatusHandler :
    BaseTradingStatusHandler {
    suspend fun handle(tradingStatus: TradingStatus)
}