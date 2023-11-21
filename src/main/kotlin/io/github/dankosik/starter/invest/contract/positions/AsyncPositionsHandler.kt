package io.github.dankosik.starter.invest.contract.positions

import ru.tinkoff.piapi.contract.v1.PositionData
import java.util.concurrent.CompletableFuture

interface AsyncPositionsHandler : BasePositionsHandler {
    fun handleAsync(positionData: PositionData): CompletableFuture<Void>
}