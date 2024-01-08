package io.github.dankosik.starter.invest.contract.marketdata.status

import ru.tinkoff.piapi.contract.v1.TradingStatus

interface BlockingTradingStatusHandler : BaseTradingStatusHandler {
    fun handleBlocking(tradingStatus: TradingStatus)
}