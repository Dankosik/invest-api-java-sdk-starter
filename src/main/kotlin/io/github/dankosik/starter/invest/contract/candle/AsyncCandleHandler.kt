package io.github.dankosik.starter.invest.contract.candle

import ru.tinkoff.piapi.contract.v1.Candle
import java.util.concurrent.CompletableFuture

interface AsyncCandleHandler : BaseCandleHandler {
    fun handleAsync(candle: Candle): CompletableFuture<Void>
}