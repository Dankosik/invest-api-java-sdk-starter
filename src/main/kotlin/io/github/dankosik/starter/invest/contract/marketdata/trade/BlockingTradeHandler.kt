package io.github.dankosik.starter.invest.contract.marketdata.trade

import ru.tinkoff.piapi.contract.v1.Trade

interface BlockingTradeHandler : BaseTradeHandler {
    fun handleBlocking(trade: Trade)
}