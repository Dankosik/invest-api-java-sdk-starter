package io.github.dankosik.starter.invest.contract.marketdata.orderbook

import ru.tinkoff.piapi.contract.v1.OrderBook

interface BlockingOrderBookHandler : BaseOrderBookHandler {
    fun handleBlocking(orderBook: OrderBook)
}