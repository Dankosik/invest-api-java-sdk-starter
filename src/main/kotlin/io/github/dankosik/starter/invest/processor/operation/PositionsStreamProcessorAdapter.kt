package io.github.dankosik.starter.invest.processor.operation

import ru.tinkoff.piapi.contract.v1.PositionsStreamResponse
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

interface BasePositionsStreamProcessor {
    var beforeEachPositionHandler: Boolean
    var afterEachPositionHandler: Boolean
    val accounts: List<String>
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

class PositionsStreamProcessorAdapterFactory {

    companion object {
        private var beforeEachPositionHandlerCompanion: Boolean = false
        private var afterEachPositionHandlerCompanion: Boolean = false
        private var accountsCompanion: List<String> = emptyList()

        @JvmStatic
        fun createBlockingHandler(consumer: Consumer<PositionsStreamResponse>): BlockingPositionsStreamProcessorAdapter =
            object : BlockingPositionsStreamProcessorAdapter {
                override fun process(positionsStreamResponse: PositionsStreamResponse) =
                    consumer.accept(positionsStreamResponse)

                override var beforeEachPositionHandler: Boolean = beforeEachPositionHandlerCompanion
                override var afterEachPositionHandler: Boolean = afterEachPositionHandlerCompanion
                override var accounts: List<String> = accountsCompanion

            }.also {
                beforeEachPositionHandlerCompanion = false
                afterEachPositionHandlerCompanion = false
                accountsCompanion = emptyList()
            }

        @JvmStatic
        fun createAsyncHandler(consumer: Function<PositionsStreamResponse, CompletableFuture<Void>>): AsyncPositionsStreamProcessorAdapter =
            object : AsyncPositionsStreamProcessorAdapter {
                override fun process(positionsStreamResponse: PositionsStreamResponse) =
                    consumer.apply(positionsStreamResponse)

                override var beforeEachPositionHandler: Boolean = beforeEachPositionHandlerCompanion
                override var afterEachPositionHandler: Boolean = afterEachPositionHandlerCompanion
                override var accounts: List<String> = accountsCompanion
            }.also {
                beforeEachPositionHandlerCompanion = false
                afterEachPositionHandlerCompanion = false
                accountsCompanion = emptyList()
            }

        @JvmStatic
        fun createCoroutineHandler(block: suspend (PositionsStreamResponse) -> Unit): CoroutinePositionsStreamProcessorAdapter =
            object : CoroutinePositionsStreamProcessorAdapter {
                override suspend fun process(positionsStreamResponse: PositionsStreamResponse): Unit =
                    block(positionsStreamResponse)

                override var beforeEachPositionHandler: Boolean = beforeEachPositionHandlerCompanion
                override var afterEachPositionHandler: Boolean = afterEachPositionHandlerCompanion
                override var accounts: List<String> = accountsCompanion
            }.also {
                beforeEachPositionHandlerCompanion = false
                afterEachPositionHandlerCompanion = false
                accountsCompanion = emptyList()
            }

        @JvmStatic
        fun runBeforeEachPositionHandler(value: Boolean): Companion {
            this.beforeEachPositionHandlerCompanion = value
            return Companion
        }

        @JvmStatic
        fun runAfterEachPositionHandler(value: Boolean): Companion {
            this.afterEachPositionHandlerCompanion = value
            return Companion
        }

        @JvmStatic
        fun withAccounts(accounts: List<String>): Companion {
            this.accountsCompanion = accounts
            return Companion
        }
    }
}