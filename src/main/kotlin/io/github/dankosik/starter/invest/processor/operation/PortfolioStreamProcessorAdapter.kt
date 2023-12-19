package io.github.dankosik.starter.invest.processor.operation

import ru.tinkoff.piapi.contract.v1.PortfolioStreamResponse
import java.util.concurrent.CompletableFuture

interface BasePortfolioStreamProcessor {
    var beforeEachPortfolioHandler: Boolean
    var afterEachPortfolioHandler: Boolean
}

interface BlockingPortfolioStreamProcessorAdapter : BasePortfolioStreamProcessor {
    fun process(portfolioStreamResponse: PortfolioStreamResponse)
}

interface AsyncPortfolioStreamProcessorAdapter : BasePortfolioStreamProcessor {
    fun process(portfolioStreamResponse: PortfolioStreamResponse): CompletableFuture<Void>
}

interface CoroutinePortfolioStreamProcessorAdapter : BasePortfolioStreamProcessor {
    suspend fun process(portfolioStreamResponse: PortfolioStreamResponse)
}

inline fun BlockingPortfolioStreamProcessorAdapter(
    crossinline block: (PortfolioStreamResponse) -> Unit
): BlockingPortfolioStreamProcessorAdapter = object : BlockingPortfolioStreamProcessorAdapter {
    override fun process(portfolioStreamResponse: PortfolioStreamResponse) = block(portfolioStreamResponse)
    override var beforeEachPortfolioHandler = false
    override var afterEachPortfolioHandler = false
}

inline fun AsyncPortfolioStreamProcessorAdapter(
    crossinline block: (PortfolioStreamResponse) -> CompletableFuture<Void>
): AsyncPortfolioStreamProcessorAdapter = object : AsyncPortfolioStreamProcessorAdapter {
    override fun process(portfolioStreamResponse: PortfolioStreamResponse): CompletableFuture<Void> =
        block(portfolioStreamResponse)

    override var beforeEachPortfolioHandler = false
    override var afterEachPortfolioHandler = false
}

inline fun CoroutinePortfolioStreamProcessorAdapter(
    crossinline block: suspend (PortfolioStreamResponse) -> Unit
): CoroutinePortfolioStreamProcessorAdapter = object : CoroutinePortfolioStreamProcessorAdapter {
    override suspend fun process(portfolioStreamResponse: PortfolioStreamResponse): Unit =
        block(portfolioStreamResponse)

    override var beforeEachPortfolioHandler = false
    override var afterEachPortfolioHandler = false
}

fun <T : BasePortfolioStreamProcessor> T.runBeforeEachPortfolioHandler(): T {
    this.beforeEachPortfolioHandler = true
    return this
}

fun <T : BasePortfolioStreamProcessor> T.runAfterEachPortfolioHandler(): T {
    this.afterEachPortfolioHandler = true
    return this
}