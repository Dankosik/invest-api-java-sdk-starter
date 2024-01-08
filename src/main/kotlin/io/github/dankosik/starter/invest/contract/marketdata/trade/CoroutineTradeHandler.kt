package io.github.dankosik.starter.invest.contract.marketdata.trade

import ru.tinkoff.piapi.contract.v1.Trade

interface CoroutineTradeHandler : BaseTradeHandler {
    suspend fun handle(trade: Trade)
}