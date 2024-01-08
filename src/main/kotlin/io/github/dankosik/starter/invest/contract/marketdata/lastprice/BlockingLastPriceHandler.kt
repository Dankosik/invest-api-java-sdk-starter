package io.github.dankosik.starter.invest.contract.marketdata.lastprice

import ru.tinkoff.piapi.contract.v1.LastPrice

interface BlockingLastPriceHandler : BaseLastPriceHandler {
    fun handleBlocking(lastPrice: LastPrice)
}