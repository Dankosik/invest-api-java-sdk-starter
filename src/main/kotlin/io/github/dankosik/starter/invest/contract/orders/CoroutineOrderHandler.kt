package io.github.dankosik.starter.invest.contract.orders

import ru.tinkoff.piapi.contract.v1.OrderTrades

interface CoroutineOrderHandler : BaseOrderHandler {
    suspend fun handle(orderTrades: OrderTrades)
}