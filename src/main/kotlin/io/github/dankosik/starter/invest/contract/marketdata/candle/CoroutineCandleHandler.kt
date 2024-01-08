package io.github.dankosik.starter.invest.contract.marketdata.candle

import ru.tinkoff.piapi.contract.v1.Candle

interface CoroutineCandleHandler : BaseCandleHandler {
    suspend fun handle(candle: Candle)
}