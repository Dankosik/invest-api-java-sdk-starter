package io.github.dankosik.starter.invest.contract.positions

import ru.tinkoff.piapi.contract.v1.PositionData

interface BlockingPositionsHandler : BasePositionsHandler {
    fun handleBlocking(positionData: PositionData)
}