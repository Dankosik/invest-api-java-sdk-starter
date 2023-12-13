package io.github.dankosik.starter.invest.configuration

import io.github.dankosik.starter.invest.configuration.properties.TinkoffApiProperties
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.annotation.Configuration
import ru.tinkoff.piapi.contract.v1.PortfolioStreamResponse
import ru.tinkoff.piapi.contract.v1.PositionsStreamResponse
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval
import ru.tinkoff.piapi.contract.v1.TradesStreamResponse
import ru.tinkoff.piapi.core.stream.MarketDataSubscriptionService
import ru.tinkoff.piapi.core.stream.OperationsStreamService
import ru.tinkoff.piapi.core.stream.OrdersStreamService
import ru.tinkoff.piapi.core.stream.StreamProcessor

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(StreamProcessorsAutoConfiguration::class)
class StreamStartAutoconfiguration(
    private val positionsStreamProcessor: StreamProcessor<PositionsStreamResponse>,
    private val ordersStreamProcessor: StreamProcessor<TradesStreamResponse>,
    private val portfolioStreamProcessor: StreamProcessor<PortfolioStreamResponse>,
    private val tinkoffApiProperties: TinkoffApiProperties,
) {

    @Autowired(required = false)
    @Qualifier("commonMarketDataSubscription")
    private val commonMarketDataSubscription: MarketDataSubscriptionService? = null

    @Autowired(required = false)
    @Qualifier("commonMarketDataSubscriptionSandbox")
    private val commonMarketDataSubscriptionSandbox: MarketDataSubscriptionService? = null

    @Autowired(required = false)
    @Qualifier("operationsStreamService")
    private val operationsStreamService: OperationsStreamService? = null

    @Autowired(required = false)
    @Qualifier("operationsStreamServiceReadonly")
    private val operationsStreamServiceReadonly: OperationsStreamService? = null

    @Autowired(required = false)
    @Qualifier("operationsStreamServiceSandbox")
    private val operationsStreamServiceSandbox: OperationsStreamService? = null

    @Autowired(required = false)
    @Qualifier("ordersStreamService")
    private val ordersStreamService: OrdersStreamService? = null

    @Autowired(required = false)
    @Qualifier("ordersStreamServiceReadonly")
    private val ordersStreamServiceReadonly: OrdersStreamService? = null

    @Autowired(required = false)
    @Qualifier("ordersStreamServiceSandbox")
    private val ordersStreamServiceSandbox: OrdersStreamService? = null

    @Autowired(required = false)
    private val instrumentsTradesSandbox: MutableSet<String>? = null

    @Autowired(required = false)
    private val instrumentsTrades: MutableSet<String>? = null

    @Autowired(required = false)
    private val instrumentsOrderBook: MutableSet<String>? = null

    @Autowired(required = false)
    private val instrumentsLastPrice: MutableSet<String>? = null

    @Autowired(required = false)
    private val instrumentsTradingStatus: MutableSet<String>? = null

    @Autowired(required = false)
    private val instrumentsCandle: MutableMap<SubscriptionInterval, MutableList<InstrumentsAutoConfiguration.InstrumentIdToWaitingClose>>? =
        null

    @Autowired(required = false)
    private val instrumentsLastPriceSandbox: MutableSet<String>? = null

    @Autowired(required = false)
    private val instrumentsOrderBookSandbox: MutableSet<String>? = null

    @Autowired(required = false)
    private val instrumentsCandleSandbox: MutableMap<SubscriptionInterval, MutableList<InstrumentsAutoConfiguration.InstrumentIdToWaitingClose>>? =
        null

    @Autowired(required = false)
    private val instrumentsTradingStatusSandbox: MutableSet<String>? = null

    @Autowired(required = false)
    private val accountsPositions: MutableSet<String>? = null

    @Autowired(required = false)
    private val accountsPositionsSandbox: MutableSet<String>? = null

    @Autowired(required = false)
    private val accountsPortfolio: MutableSet<String>? = null

    @Autowired(required = false)
    private val accountsPortfolioSandbox: MutableSet<String>? = null


    @Autowired(required = false)
    private val accountsOrders: MutableSet<String>? = null

    @Autowired(required = false)
    private val accountsOrdersSandbox: MutableSet<String>? = null


    @PostConstruct
    fun init() {
        subscribeMarketDataStream()
        subscribeMarketDataStreamSandbox()

        subscribeOperations()
        subscribeOperationsSandbox()

        subscribeOrders()

        subscribeOrdersSandbox()
    }

    private fun subscribeOrdersSandbox() {
        ordersStreamServiceSandbox?.subscribeTrades(ordersStreamProcessor, accountsOrdersSandbox!!.toList())
    }

    private fun subscribeOrders() {
        accountsOrders.takeIf { !it.isNullOrEmpty() }?.toList()
            ?.let {
                ordersStreamService?.subscribeTrades(ordersStreamProcessor, it)
                    ?: ordersStreamServiceReadonly?.subscribeTrades(ordersStreamProcessor, it)
            }
    }

    private fun subscribeOperationsSandbox() {
        accountsPortfolioSandbox.takeIf { !it.isNullOrEmpty() }?.toList()
            ?.let {
                operationsStreamServiceSandbox?.subscribePortfolio(portfolioStreamProcessor, it)
            }

        accountsPositionsSandbox.takeIf { !it.isNullOrEmpty() }?.toList()
            ?.let {
                operationsStreamServiceSandbox?.subscribePositions(positionsStreamProcessor, it)
            }

    }

    private fun subscribeOperations() {
        accountsPositions.takeIf { !it.isNullOrEmpty() }?.toList()
            ?.let {
                operationsStreamService?.subscribePositions(positionsStreamProcessor, it)
                    ?: operationsStreamServiceReadonly?.subscribePositions(positionsStreamProcessor, it)
            }
        accountsPortfolio.takeIf { !it.isNullOrEmpty() }?.toList()
            ?.let {
                operationsStreamService?.subscribePortfolio(portfolioStreamProcessor, it)
                    ?: operationsStreamServiceReadonly?.subscribePortfolio(portfolioStreamProcessor, it)
            }
    }

    private fun subscribeMarketDataStreamSandbox() {
        instrumentsCandleSandbox?.forEach { intervalToInstrumentsMap ->
            intervalToInstrumentsMap.value.filter { it.waitingClose }
                .map { it.instrumentId }
                .takeIf { it.isNotEmpty() }
                ?.let {
                    commonMarketDataSubscriptionSandbox?.subscribeCandles(it, intervalToInstrumentsMap.key, true)
                }
            intervalToInstrumentsMap.value.filter { !it.waitingClose }
                .map { it.instrumentId }
                .takeIf { it.isNotEmpty() }
                ?.let {
                    commonMarketDataSubscriptionSandbox?.subscribeCandles(it, intervalToInstrumentsMap.key, false)
                }
        }
        instrumentsTradesSandbox.takeIf { !it.isNullOrEmpty() }?.toList()
            ?.let { commonMarketDataSubscriptionSandbox?.subscribeTrades(it) }
        instrumentsLastPriceSandbox.takeIf { !it.isNullOrEmpty() }?.toList()
            ?.let { commonMarketDataSubscriptionSandbox?.subscribeLastPrices(it) }
        instrumentsOrderBookSandbox.takeIf { !it.isNullOrEmpty() }?.toList()
            ?.let {
                val depth = tinkoffApiProperties.subscription?.orderBook?.depth ?: DEFAULT_ORDERBOOK_DEPTH
                commonMarketDataSubscriptionSandbox?.subscribeOrderbook(it, depth)
            }
        instrumentsTradingStatusSandbox.takeIf { !it.isNullOrEmpty() }?.toList()
            ?.let { commonMarketDataSubscriptionSandbox?.subscribeInfo(it) }
    }

    private fun subscribeMarketDataStream() {
        instrumentsCandle?.forEach { intervalToInstrumentsMap ->
            intervalToInstrumentsMap.value.filter { it.waitingClose }
                .map { it.instrumentId }
                .takeIf { it.isNotEmpty() }
                ?.let {
                    commonMarketDataSubscription?.subscribeCandles(it, intervalToInstrumentsMap.key, true)
                }
            intervalToInstrumentsMap.value.filter { !it.waitingClose }
                .map { it.instrumentId }
                .takeIf { it.isNotEmpty() }
                ?.let {
                    commonMarketDataSubscription?.subscribeCandles(it, intervalToInstrumentsMap.key, false)
                }
        }
        instrumentsTrades.takeIf { !it.isNullOrEmpty() }?.toList()
            ?.let { commonMarketDataSubscription?.subscribeTrades(it) }
        instrumentsLastPrice.takeIf { !it.isNullOrEmpty() }?.toList()
            ?.let { commonMarketDataSubscription?.subscribeLastPrices(it) }
        instrumentsOrderBook.takeIf { !it.isNullOrEmpty() }?.toList()
            ?.let {
                val depth = tinkoffApiProperties.subscription?.orderBook?.depth ?: DEFAULT_ORDERBOOK_DEPTH
                commonMarketDataSubscription?.subscribeOrderbook(it, depth)
            }
        instrumentsTradingStatus.takeIf { !it.isNullOrEmpty() }?.toList()
            ?.let { commonMarketDataSubscription?.subscribeInfo(it) }
    }

    private companion object {
        private const val DEFAULT_ORDERBOOK_DEPTH = 50
    }
}