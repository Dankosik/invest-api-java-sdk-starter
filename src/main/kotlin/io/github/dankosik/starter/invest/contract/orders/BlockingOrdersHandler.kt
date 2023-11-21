package io.github.dankosik.starter.invest.contract.orders

import ru.tinkoff.piapi.contract.v1.OrderTrades

interface BlockingOrdersHandler : BaseOrdersHandler {
    fun handleBlocking(orderTrades: OrderTrades)
}