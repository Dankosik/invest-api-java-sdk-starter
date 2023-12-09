package io.github.dankosik.starter.invest.processor.order

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

inline fun BlockingOrdersStreamProcessorAdapter(
    crossinline block: (TradesStreamResponse) -> Unit
): BlockingOrdersStreamProcessorAdapter = object : BlockingOrdersStreamProcessorAdapter {
    override fun process(tradesStreamResponse: TradesStreamResponse) = block(tradesStreamResponse)
    override var beforeOrdersHandlers = false
    override var afterOrdersHandlers = false
}


inline fun AsyncOrdersStreamProcessorAdapter(
    crossinline block: (TradesStreamResponse) -> CompletableFuture<Void>
): AsyncOrdersStreamProcessorAdapter = object : AsyncOrdersStreamProcessorAdapter {
    override fun process(tradesStreamResponse: TradesStreamResponse): CompletableFuture<Void> =
        block(tradesStreamResponse)

    override var beforeOrdersHandlers = false
    override var afterOrdersHandlers = false
}

inline fun CoroutineOrdersStreamProcessorAdapter(
    crossinline block: suspend (TradesStreamResponse) -> Unit
): CoroutineOrdersStreamProcessorAdapter = object : CoroutineOrdersStreamProcessorAdapter {
    override suspend fun process(tradesStreamResponse: TradesStreamResponse): Unit =
        block(tradesStreamResponse)

    override var beforeOrdersHandlers = false
    override var afterOrdersHandlers = false
}

fun <T : BaseOrdersStreamProcessor> T.runBeforePositionsHandlers(): T {
    this.beforeOrdersHandlers = true
    return this
}

fun <T : BaseOrdersStreamProcessor> T.runAfterPositionsHandlers(): T {
    this.afterOrdersHandlers = true
    return this
}