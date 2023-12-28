package io.github.dankosik.starter.invest.processor.operation

import ru.tinkoff.piapi.contract.v1.PortfolioStreamResponse
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

interface BasePortfolioStreamProcessor {
    var beforeEachPortfolioHandler: Boolean
    var afterEachPortfolioHandler: Boolean
    val accounts: List<String>
}

fun List<BasePortfolioStreamProcessor>.toHandlersByAccount() =
    associateBy(
        keySelector = { it.accounts },
        valueTransform = { it }
    ).transformMap()

private fun Map<List<String>, BasePortfolioStreamProcessor>.transformMap(): Map<String, List<BasePortfolioStreamProcessor>> =
    flatMap { (keys, value) ->
        keys.map { key -> key to value }
    }.groupBy({ it.first }, { it.second })

interface BlockingPortfolioStreamProcessorAdapter : BasePortfolioStreamProcessor {
    fun process(portfolioStreamResponse: PortfolioStreamResponse)
}

interface AsyncPortfolioStreamProcessorAdapter : BasePortfolioStreamProcessor {
    fun process(portfolioStreamResponse: PortfolioStreamResponse): CompletableFuture<Void>
}

interface CoroutinePortfolioStreamProcessorAdapter : BasePortfolioStreamProcessor {
    suspend fun process(portfolioStreamResponse: PortfolioStreamResponse)
}

class PortfolioStreamProcessorAdapterFactory {

    companion object {
        private var beforeEachPortfolioHandlerCompanion: Boolean = false
        private var afterEachPortfolioHandlerCompanion: Boolean = false
        private var accountsCompanion: List<String> = emptyList()

        @JvmStatic
        fun createBlockingHandler(consumer: Consumer<PortfolioStreamResponse>): BlockingPortfolioStreamProcessorAdapter =
            object : BlockingPortfolioStreamProcessorAdapter {
                override fun process(portfolioStreamResponse: PortfolioStreamResponse) =
                    consumer.accept(portfolioStreamResponse)

                override var beforeEachPortfolioHandler: Boolean = beforeEachPortfolioHandlerCompanion
                override var afterEachPortfolioHandler: Boolean = afterEachPortfolioHandlerCompanion
                override var accounts: List<String> = accountsCompanion

            }.also {
                beforeEachPortfolioHandlerCompanion = false
                afterEachPortfolioHandlerCompanion = false
                accountsCompanion = emptyList()
            }

        @JvmStatic
        fun createAsyncHandler(consumer: Function<PortfolioStreamResponse, CompletableFuture<Void>>): AsyncPortfolioStreamProcessorAdapter =
            object : AsyncPortfolioStreamProcessorAdapter {
                override fun process(portfolioStreamResponse: PortfolioStreamResponse) =
                    consumer.apply(portfolioStreamResponse)

                override var beforeEachPortfolioHandler: Boolean = beforeEachPortfolioHandlerCompanion
                override var afterEachPortfolioHandler: Boolean = afterEachPortfolioHandlerCompanion
                override var accounts: List<String> = accountsCompanion
            }.also {
                beforeEachPortfolioHandlerCompanion = false
                afterEachPortfolioHandlerCompanion = false
                accountsCompanion = emptyList()
            }

        @JvmStatic
        fun createCoroutineHandler(block: suspend (PortfolioStreamResponse) -> Unit): CoroutinePortfolioStreamProcessorAdapter =
            object : CoroutinePortfolioStreamProcessorAdapter {
                override suspend fun process(portfolioStreamResponse: PortfolioStreamResponse): Unit =
                    block(portfolioStreamResponse)

                override var beforeEachPortfolioHandler: Boolean = beforeEachPortfolioHandlerCompanion
                override var afterEachPortfolioHandler: Boolean = afterEachPortfolioHandlerCompanion
                override var accounts: List<String> = accountsCompanion
            }.also {
                beforeEachPortfolioHandlerCompanion = false
                afterEachPortfolioHandlerCompanion = false
                accountsCompanion = emptyList()
            }

        @JvmStatic
        fun runBeforeEachPortfolioHandler(value: Boolean): Companion {
            this.beforeEachPortfolioHandlerCompanion = value
            return Companion
        }

        @JvmStatic
        fun runAfterEachPortfolioHandler(value: Boolean): Companion {
            this.afterEachPortfolioHandlerCompanion = value
            return Companion
        }

        @JvmStatic
        fun withAccounts(accounts: List<String>): Companion {
            this.accountsCompanion = accounts
            return Companion
        }
    }
}