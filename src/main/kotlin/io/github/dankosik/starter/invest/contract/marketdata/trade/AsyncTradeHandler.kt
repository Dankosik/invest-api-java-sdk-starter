package io.github.dankosik.starter.invest.contract.marketdata.trade

import ru.tinkoff.piapi.contract.v1.Trade
import java.util.concurrent.CompletableFuture

interface AsyncTradeHandler : BaseTradeHandler {
    fun handleAsync(trade: Trade): CompletableFuture<Void>
}