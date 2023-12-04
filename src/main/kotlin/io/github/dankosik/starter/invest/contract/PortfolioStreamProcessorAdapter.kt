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

inline fun BlockingPortfolioStreamProcessorAdapter(
    crossinline block: (PortfolioStreamResponse) -> Unit
): BlockingPortfolioStreamProcessorAdapter = object : BlockingPortfolioStreamProcessorAdapter {
    override fun process(portfolioStreamResponse: PortfolioStreamResponse) = block(portfolioStreamResponse)
    override var beforePortfolioHandlers = false
    override var afterPortfolioHandlers = false
}


inline fun AsyncPortfolioStreamProcessorAdapter(
    crossinline block: (PortfolioStreamResponse) -> CompletableFuture<Void>
): AsyncPortfolioStreamProcessorAdapter = object : AsyncPortfolioStreamProcessorAdapter {
    override fun process(portfolioStreamResponse: PortfolioStreamResponse): CompletableFuture<Void> =
        block(portfolioStreamResponse)

    override var beforePortfolioHandlers = false
    override var afterPortfolioHandlers = false
}

inline fun CoroutinePortfolioStreamProcessorAdapter(
    crossinline block: suspend (PortfolioStreamResponse) -> Unit
): CoroutinePortfolioStreamProcessorAdapter = object : CoroutinePortfolioStreamProcessorAdapter {
    override suspend fun process(portfolioStreamResponse: PortfolioStreamResponse): Unit =
        block(portfolioStreamResponse)

    override var beforePortfolioHandlers = false
    override var afterPortfolioHandlers = false
}

fun <T : BasePortfolioStreamProcessor> T.runBeforePortfolioHandlers(): T {
    this.beforePortfolioHandlers = true
    return this
}

fun <T : BasePortfolioStreamProcessor> T.runAfterPortfolioHandlers(): T {
    this.afterPortfolioHandlers = true
    return this
}