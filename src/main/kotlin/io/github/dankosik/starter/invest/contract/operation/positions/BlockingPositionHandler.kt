package io.github.dankosik.starter.invest.contract.operation.positions

import ru.tinkoff.piapi.contract.v1.PositionData

interface BlockingPositionHandler : BasePositionHandler {
    fun handleBlocking(positionData: PositionData)
}