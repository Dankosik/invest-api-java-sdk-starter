package io.github.dankosik.starter.invest.contract

import ru.tinkoff.piapi.contract.v1.TradesStreamResponse
import java.util.concurrent.CompletableFuture

interface BaseOrdersStreamProcessor {
    var beforeOrdersHandlers: Boolean
    var afterOrdersHandlers: Boolean
}

interface BlockingOrdersStreamProcessorAdapter : BaseOrdersStreamProcessor {
    fun process(tradesStreamResponse: TradesStreamResponse)
}

interface AsyncOrdersStreamProcessorAdapter : BaseOrdersStreamProcessor {
    fun process(tradesStreamResponse: TradesStreamResponse): CompletableFuture<Void>
}

interface CoroutineOrdersStreamProcessorAdapter : BaseOrdersStreamProcessor {
    suspend fun process(tradesStreamResponse: TradesStreamResponse)
}

fun BlockingOrdersStreamProcessorAdapter(
    block: (TradesStreamResponse) -> Unit
): BlockingOrdersStreamProcessorAdapter = object : BlockingOrdersStreamProcessorAdapter {
    override fun process(tradesStreamResponse: TradesStreamResponse) = block(tradesStreamResponse)
    override var beforeOrdersHandlers = false
    override var afterOrdersHandlers = false
}


fun AsyncOrdersStreamProcessorAdapter(
    block: (TradesStreamResponse) -> CompletableFuture<Void>
): AsyncOrdersStreamProcessorAdapter = object : AsyncOrdersStreamProcessorAdapter {
    override fun process(tradesStreamResponse: TradesStreamResponse): CompletableFuture<Void> =
        block(tradesStreamResponse)

    override var beforeOrdersHandlers = false
    override var afterOrdersHandlers = false
}

fun CoroutineOrdersStreamProcessorAdapter(
    block: suspend (TradesStreamResponse) -> Unit
): CoroutineOrdersStreamProcessorAdapter = object : CoroutineOrdersStreamProcessorAdapter {
    override suspend fun process(tradesStreamResponse: TradesStreamResponse): Unit =
        block(tradesStreamResponse)

    override var beforeOrdersHandlers = false
    override var afterOrdersHandlers = false
}

fun <T : BaseOrdersStreamProcessor> T.beforePositionsHandlers(): T {
    this.beforeOrdersHandlers = true
    return this
}

fun <T : BaseOrdersStreamProcessor> T.afterPositionsHandlers(): T {
    this.afterOrdersHandlers = true
    return this
}