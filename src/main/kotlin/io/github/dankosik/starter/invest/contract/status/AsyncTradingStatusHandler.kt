package io.github.dankosik.starter.invest.contract.status

import ru.tinkoff.piapi.contract.v1.TradingStatus
import java.util.concurrent.CompletableFuture

interface AsyncTradingStatusHandler : BaseTradingStatusHandler {
    fun handleAsync(tradingStatus: TradingStatus): CompletableFuture<Void>
}