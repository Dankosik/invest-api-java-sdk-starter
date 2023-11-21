package io.github.dankosik.starter.invest.contract.trade

import ru.tinkoff.piapi.contract.v1.Trade

interface BlockingTradesHandler : BaseTradesHandler {
    fun handleBlocking(trade: Trade)
}