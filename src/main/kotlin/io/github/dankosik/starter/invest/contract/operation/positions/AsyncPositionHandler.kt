package io.github.dankosik.starter.invest.contract.operation.positions

import ru.tinkoff.piapi.contract.v1.PositionData
import java.util.concurrent.CompletableFuture

interface AsyncPositionHandler : BasePositionHandler {
    fun handleAsync(positionData: PositionData): CompletableFuture<Void>
}