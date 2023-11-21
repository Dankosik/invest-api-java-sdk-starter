package io.github.dankosik.starter.invest.contract.orders

import ru.tinkoff.piapi.contract.v1.OrderTrades
import java.util.concurrent.CompletableFuture

interface AsyncOrdersHandler : BaseOrdersHandler {
    fun handleAsync(orderTrades: OrderTrades): CompletableFuture<Void>
}