package io.github.dankosik.starter.invest.contract.trade

import ru.tinkoff.piapi.contract.v1.Trade
import java.util.concurrent.CompletableFuture

interface AsyncTradesHandler : BaseTradesHandler {
    fun handleAsync(trade: Trade): CompletableFuture<Void>
}