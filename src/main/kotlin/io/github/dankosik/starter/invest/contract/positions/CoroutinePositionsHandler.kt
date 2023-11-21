package io.github.dankosik.starter.invest.contract.positions

import ru.tinkoff.piapi.contract.v1.PositionData

interface CoroutinePositionsHandler : BasePositionsHandler {
    suspend fun handle(positionData: PositionData)
}