package io.github.dankosik.starter.invest.validation.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllCandles
import io.github.dankosik.starter.invest.annotation.marketdata.HandleCandle
import io.github.dankosik.starter.invest.contract.marketdata.candle.AsyncCandleHandler
import io.github.dankosik.starter.invest.contract.marketdata.candle.BlockingCandleHandler
import io.github.dankosik.starter.invest.contract.marketdata.candle.CoroutineCandleHandler
import org.springframework.beans.factory.config.BeanPostProcessor
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

internal class CandleBeanPostProcessor : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandleCandle = bean.javaClass.declaredAnnotations.filterIsInstance<HandleCandle>().isNotEmpty()
        val isAllHandleCandles = bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllCandles>().isNotEmpty()
        val javaClassName = bean.javaClass.name
        check(
            !(isHandleCandle
                    && (bean !is CoroutineCandleHandler && bean !is BlockingCandleHandler && bean !is AsyncCandleHandler))
        ) { "Class: $javaClassName annotated with HandleCandle should implement AsyncCandleHandler or BlockingCandleHandler or CoroutineCandleHandler" }
        check(
            !(isAllHandleCandles
                    && (bean !is CoroutineCandleHandler && bean !is BlockingCandleHandler && bean !is AsyncCandleHandler))
        ) { "Class: $javaClassName annotated with HandleCandle should implement AsyncCandleHandler or BlockingCandleHandler or CoroutineCandleHandler" }
        if (isHandleCandle) {
            val classNameInMessage = when (bean) {
                is CoroutineCandleHandler -> "CoroutineCandleHandler"
                is BlockingCandleHandler -> "BlockingCandleHandler"
                else -> "AsyncCandleHandler"
            }
            check(isHandleCandle) {
                "$classNameInMessage: $javaClassName must have an annotated of HandleCandle"
            }
            val annotation = bean.javaClass.getAnnotation(HandleCandle::class.java)
            val tickerValue = annotation.ticker
            val figiValue = annotation.figi
            val instrumentIdValue = annotation.instrumentUid
            val subscriptionInterval = annotation.subscriptionInterval
            check(tickerValue.isNotBlank() || figiValue.isNotBlank() || instrumentIdValue.isNotBlank()) {
                "At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided in $javaClassName"
            }
            check(subscriptionInterval != SubscriptionInterval.SUBSCRIPTION_INTERVAL_UNSPECIFIED) {
                "SubscriptionInterval is required for CandleHandler $javaClassName"
            }
        }

        if (isAllHandleCandles) {
            val annotation = bean.javaClass.getAnnotation(HandleAllCandles::class.java)

            annotation.figies.takeIf { it.isNotEmpty() }?.forEach { figi ->
                check(figi.isNotBlank()) {
                    "$javaClassName: Figi should be not blank"
                }
            }
            annotation.tickers.takeIf { it.isNotEmpty() }?.forEach { ticker ->
                check(ticker.isNotBlank()) {
                    "$javaClassName: Ticker should be not blank"
                }
            }
            annotation.instrumentsUids.takeIf { it.isNotEmpty() }?.forEach { uId ->
                check(uId.isNotBlank()) {
                    "$javaClassName: InstrumentsUid should be not blank"
                }
            }
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean
}
