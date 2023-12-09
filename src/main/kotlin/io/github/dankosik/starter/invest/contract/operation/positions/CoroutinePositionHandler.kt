package io.github.dankosik.starter.invest.contract.operation.positions

import ru.tinkoff.piapi.contract.v1.PositionData

interface CoroutinePositionHandler : BasePositionHandler {
    suspend fun handle(positionData: PositionData)
}