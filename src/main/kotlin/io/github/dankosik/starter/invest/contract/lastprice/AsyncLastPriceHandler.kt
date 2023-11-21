package io.github.dankosik.starter.invest.contract.lastprice

import ru.tinkoff.piapi.contract.v1.LastPrice
import java.util.concurrent.CompletableFuture

interface AsyncLastPriceHandler : BaseLastPriceHandler {
    fun handleAsync(lastPrice: LastPrice): CompletableFuture<Void>
}