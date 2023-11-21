package io.github.dankosik.starter.invest.contract

import ru.tinkoff.piapi.contract.v1.PortfolioStreamResponse
import java.util.concurrent.CompletableFuture

interface BasePortfolioStreamProcessor {
    var beforePortfolioHandlers: Boolean
    var afterPortfolioHandlers: Boolean
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

fun BlockingPortfolioStreamProcessorAdapter(
    block: (PortfolioStreamResponse) -> Unit
): BlockingPortfolioStreamProcessorAdapter = object : BlockingPortfolioStreamProcessorAdapter {
    override fun process(portfolioStreamResponse: PortfolioStreamResponse) = block(portfolioStreamResponse)
    override var beforePortfolioHandlers = false
    override var afterPortfolioHandlers = false
}


fun AsyncPortfolioStreamProcessorAdapter(
    block: (PortfolioStreamResponse) -> CompletableFuture<Void>
): AsyncPortfolioStreamProcessorAdapter = object : AsyncPortfolioStreamProcessorAdapter {
    override fun process(portfolioStreamResponse: PortfolioStreamResponse): CompletableFuture<Void> =
        block(portfolioStreamResponse)

    override var beforePortfolioHandlers = false
    override var afterPortfolioHandlers = false
}

fun CoroutinePortfolioStreamProcessorAdapter(
    block: suspend (PortfolioStreamResponse) -> Unit
): CoroutinePortfolioStreamProcessorAdapter = object : CoroutinePortfolioStreamProcessorAdapter {
    override suspend fun process(portfolioStreamResponse: PortfolioStreamResponse): Unit =
        block(portfolioStreamResponse)

    override var beforePortfolioHandlers = false
    override var afterPortfolioHandlers = false
}

fun <T : BasePortfolioStreamProcessor> T.beforePortfolioHandlers(): T {
    this.beforePortfolioHandlers = true
    return this
}

fun <T : BasePortfolioStreamProcessor> T.afterPortfolioHandlers(): T {
    this.afterPortfolioHandlers = true
    return this
}