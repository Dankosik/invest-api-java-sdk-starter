package io.github.dankosik.starter.invest.contract.orders

import ru.tinkoff.piapi.contract.v1.OrderTrades

interface BlockingOrderHandler : BaseOrderHandler {
    fun handleBlocking(orderTrades: OrderTrades)
}