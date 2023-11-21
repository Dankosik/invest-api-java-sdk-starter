package io.github.dankosik.starter.invest.contract

import ru.tinkoff.piapi.contract.v1.PositionsStreamResponse
import java.util.concurrent.CompletableFuture

interface BasePositionsStreamProcessor {
    var beforePositionsHandlers: Boolean
    var afterPositionsHandlers: Boolean
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

fun BlockingPositionsStreamProcessorAdapter(
    block: (PositionsStreamResponse) -> Unit
): BlockingPositionsStreamProcessorAdapter = object : BlockingPositionsStreamProcessorAdapter {
    override fun process(positionsStreamResponse: PositionsStreamResponse) = block(positionsStreamResponse)
    override var beforePositionsHandlers = false
    override var afterPositionsHandlers = false
}


fun AsyncPositionsStreamProcessorAdapter(
    block: (PositionsStreamResponse) -> CompletableFuture<Void>
): AsyncPositionsStreamProcessorAdapter = object : AsyncPositionsStreamProcessorAdapter {
    override fun process(positionsStreamResponse: PositionsStreamResponse): CompletableFuture<Void> =
        block(positionsStreamResponse)

    override var beforePositionsHandlers = false
    override var afterPositionsHandlers = false
}

fun CoroutinePositionsStreamProcessorAdapter(
    block: suspend (PositionsStreamResponse) -> Unit
): CoroutinePositionsStreamProcessorAdapter = object : CoroutinePositionsStreamProcessorAdapter {
    override suspend fun process(positionsStreamResponse: PositionsStreamResponse): Unit =
        block(positionsStreamResponse)

    override var beforePositionsHandlers = false
    override var afterPositionsHandlers = false
}

fun <T : BasePositionsStreamProcessor> T.beforePositionsHandlers(): T {
    this.beforePositionsHandlers = true
    return this
}

fun <T : BasePositionsStreamProcessor> T.afterPositionsHandlers(): T {
    this.afterPositionsHandlers = true
    return this
}