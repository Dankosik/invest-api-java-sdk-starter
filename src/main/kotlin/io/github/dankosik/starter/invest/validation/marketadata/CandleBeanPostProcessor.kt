package io.github.dankosik.starter.invest.validation.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllCandles
import io.github.dankosik.starter.invest.annotation.marketdata.HandleCandle
import io.github.dankosik.starter.invest.contract.marketdata.candle.AsyncCandleHandler
import io.github.dankosik.starter.invest.contract.marketdata.candle.BlockingCandleHandler
import io.github.dankosik.starter.invest.contract.marketdata.candle.CoroutineCandleHandler
import mu.KLogging
import org.springframework.beans.factory.config.BeanPostProcessor
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

internal class CandleBeanPostProcessor : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandleCandle = bean.javaClass.declaredAnnotations.filterIsInstance<HandleCandle>().isNotEmpty()
        val isAllHandleCandles = bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllCandles>().isNotEmpty()
        check(
            !(isHandleCandle
                    && (bean !is CoroutineCandleHandler && bean !is BlockingCandleHandler && bean !is AsyncCandleHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleCandle should be implements AsyncCandleHandler or BlockingCandleHandler or CoroutineCandleHandler" }
        check(
            !(isAllHandleCandles
                    && (bean !is CoroutineCandleHandler && bean !is BlockingCandleHandler && bean !is AsyncCandleHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleAllCandles should be implements AsyncCandleHandler or BlockingCandleHandler or CoroutineCandleHandler" }
        if (isHandleCandle) {
            val classNameInMessage = when (bean) {
                is CoroutineCandleHandler -> "CoroutineCandleHandler"
                is BlockingCandleHandler -> "BlockingCandleHandler"
                else -> "AsyncCandleHandler"
            }
            check(isHandleCandle) {
                "$classNameInMessage: ${bean.javaClass.name} must have an annotated of HandleCandle"
            }
            val annotation = bean.javaClass.getAnnotation(HandleCandle::class.java)
            val tickerValue = annotation.ticker
            val figiValue = annotation.figi
            val instrumentIdValue = annotation.instrumentUid
            val subscriptionInterval = annotation.subscriptionInterval
            check(tickerValue.isNotBlank() || figiValue.isNotBlank() || instrumentIdValue.isNotBlank()) {
                "At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided in ${bean.javaClass.name}"
            }
            check(subscriptionInterval != SubscriptionInterval.SUBSCRIPTION_INTERVAL_UNSPECIFIED) {
                "SubscriptionInterval is required for CandleHandler ${bean.javaClass.name}"
            }
        }

        if (isAllHandleCandles) {
            val annotation = bean.javaClass.getAnnotation(HandleAllCandles::class.java)
            if (annotation.beforeEachCandleHandler && annotation.afterEachCandleHandler) {
                logger.warn { "${bean.javaClass.name} any parameters of annotation 'HandleAllCandles' should be true, your handler will be ignored" }
            }
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean

    private companion object : KLogging()
}
