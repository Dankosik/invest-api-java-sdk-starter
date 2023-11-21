package io.github.dankosik.starter.invest.contract.trade

import ru.tinkoff.piapi.contract.v1.Trade

interface CoroutineTradesHandler : BaseTradesHandler {
    suspend fun handle(trade: Trade)
}