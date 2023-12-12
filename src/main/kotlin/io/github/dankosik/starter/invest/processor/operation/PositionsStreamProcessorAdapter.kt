package io.github.dankosik.starter.invest.processor.operation

import ru.tinkoff.piapi.contract.v1.PositionsStreamResponse
import java.util.concurrent.CompletableFuture

interface BasePositionsStreamProcessor {
    var beforeEachPositionHandler: Boolean
    var afterEachPositionHandler: Boolean
}

interface BlockingPositionsStreamProcessorAdapter : BasePositionsStreamProcessor {
    fun process(positionsStreamResponse: PositionsStreamResponse)
}

interface AsyncPositionsStreamProcessorAdapter : BasePositionsStreamProcessor {
    fun process(positionsStreamResponse: PositionsStreamResponse): CompletableFuture<Void>
}

interface CoroutinePositionsStreamProcessorAdapter : BasePositionsStreamProcessor {
    suspend fun process(positionsStreamResponse: PositionsStreamResponse)
}

inline fun BlockingPositionsStreamProcessorAdapter(
    crossinline block: (PositionsStreamResponse) -> Unit
): BlockingPositionsStreamProcessorAdapter = object : BlockingPositionsStreamProcessorAdapter {
    override fun process(positionsStreamResponse: PositionsStreamResponse) = block(positionsStreamResponse)
    override var beforeEachPositionHandler = false
    override var afterEachPositionHandler = false
}


inline fun AsyncPositionsStreamProcessorAdapter(
    crossinline block: (PositionsStreamResponse) -> CompletableFuture<Void>
): AsyncPositionsStreamProcessorAdapter = object : AsyncPositionsStreamProcessorAdapter {
    override fun process(positionsStreamResponse: PositionsStreamResponse): CompletableFuture<Void> =
        block(positionsStreamResponse)

    override var beforeEachPositionHandler = false
    override var afterEachPositionHandler = false
}

inline fun CoroutinePositionsStreamProcessorAdapter(
    crossinline block: suspend (PositionsStreamResponse) -> Unit
): CoroutinePositionsStreamProcessorAdapter = object : CoroutinePositionsStreamProcessorAdapter {
    override suspend fun process(positionsStreamResponse: PositionsStreamResponse): Unit =
        block(positionsStreamResponse)

    override var beforeEachPositionHandler = false
    override var afterEachPositionHandler = false
}

fun <T : BasePositionsStreamProcessor> T.runBeforeEachPositionHandler(): T {
    this.beforeEachPositionHandler = true
    return this
}

fun <T : BasePositionsStreamProcessor> T.runAfterEachPositionHandler(): T {
    this.afterEachPositionHandler = true
    return this
}