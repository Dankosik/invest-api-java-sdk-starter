package io.github.dankosik.starter.invest.processor.order

import ru.tinkoff.piapi.contract.v1.TradesStreamResponse
import java.util.concurrent.CompletableFuture

interface BaseOrdersStreamProcessor {
    var beforeEachOrdersHandler: Boolean
    var afterEachOrdersHandler: Boolean
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
    override var beforeEachOrdersHandler = false
    override var afterEachOrdersHandler = false
}


inline fun AsyncOrdersStreamProcessorAdapter(
    crossinline block: (TradesStreamResponse) -> CompletableFuture<Void>
): AsyncOrdersStreamProcessorAdapter = object : AsyncOrdersStreamProcessorAdapter {
    override fun process(tradesStreamResponse: TradesStreamResponse): CompletableFuture<Void> =
        block(tradesStreamResponse)

    override var beforeEachOrdersHandler = false
    override var afterEachOrdersHandler = false
}

inline fun CoroutineOrdersStreamProcessorAdapter(
    crossinline block: suspend (TradesStreamResponse) -> Unit
): CoroutineOrdersStreamProcessorAdapter = object : CoroutineOrdersStreamProcessorAdapter {
    override suspend fun process(tradesStreamResponse: TradesStreamResponse): Unit =
        block(tradesStreamResponse)

    override var beforeEachOrdersHandler = false
    override var afterEachOrdersHandler = false
}

fun <T : BaseOrdersStreamProcessor> T.runBeforeEachOrdersHandle(): T {
    this.beforeEachOrdersHandler = true
    return this
}

fun <T : BaseOrdersStreamProcessor> T.runAfterEachOrdersHandler(): T {
    this.afterEachOrdersHandler = true
    return this
}