package io.github.dankosik.starter.invest.contract.lastprice

import ru.tinkoff.piapi.contract.v1.LastPrice

interface CoroutineLastPriceHandler : BaseLastPriceHandler {
    suspend fun handle(lastPrice: LastPrice)
}