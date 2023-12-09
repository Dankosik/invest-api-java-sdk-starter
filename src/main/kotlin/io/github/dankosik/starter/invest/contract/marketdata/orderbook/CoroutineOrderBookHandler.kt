package io.github.dankosik.starter.invest.contract.marketdata.orderbook

import ru.tinkoff.piapi.contract.v1.OrderBook

interface CoroutineOrderBookHandler : BaseOrderBookHandler {
    suspend fun handle(orderBook: OrderBook)
}