package io.github.dankosik.starter.invest.contract.marketdata.orderbook

import ru.tinkoff.piapi.contract.v1.OrderBook
import java.util.concurrent.CompletableFuture

interface AsyncOrderBookHandler : BaseOrderBookHandler {
    fun handleAsync(orderBook: OrderBook): CompletableFuture<Void>
}