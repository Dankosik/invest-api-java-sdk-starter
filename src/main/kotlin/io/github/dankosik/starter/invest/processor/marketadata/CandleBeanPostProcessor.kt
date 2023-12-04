package io.github.dankosik.starter.invest.processor.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleCandle
import io.github.dankosik.starter.invest.contract.candle.AsyncCandleHandler
import io.github.dankosik.starter.invest.contract.candle.BlockingCandleHandler
import io.github.dankosik.starter.invest.contract.candle.CoroutineCandleHandler
import org.springframework.beans.factory.config.BeanPostProcessor
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

internal class CandleBeanPostProcessor : BeanPostProcessor {

    private val uniquePairs = mutableSetOf<Pair<String, SubscriptionInterval>>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        check(
            !(bean.javaClass.declaredAnnotations.filterIsInstance<HandleCandle>().isNotEmpty()
                    && (bean !is CoroutineCandleHandler && bean !is BlockingCandleHandler && bean !is AsyncCandleHandler))
        ) { "Class: ${bean.javaClass.name} that annotated of HandleCandle should be implements AsyncCandleHandler or BlockingCandleHandler or CoroutineCandleHandler" }
        if (bean is CoroutineCandleHandler || bean is BlockingCandleHandler || bean is AsyncCandleHandler) {
            val classNameInMessage = when (bean) {
                is CoroutineCandleHandler -> "CoroutineCandleHandler"
                is BlockingCandleHandler -> "BlockingCandleHandler"
                else -> "AsyncCandleHandler"
            }
            check(bean.javaClass.declaredAnnotations.filterIsInstance<HandleCandle>().isNotEmpty()) {
                "$classNameInMessage: ${bean.javaClass.name} must have an annotated of HandleCandle"
            }
            val tickerValue = bean.javaClass.getAnnotation(HandleCandle::class.java).ticker
            val figiValue = bean.javaClass.getAnnotation(HandleCandle::class.java).figi
            val instrumentIdValue = bean.javaClass.getAnnotation(HandleCandle::class.java).instrumentUid
            val subscriptionInterval = bean.javaClass.getAnnotation(HandleCandle::class.java).subscriptionInterval
            check(tickerValue.isNotBlank() || figiValue.isNotBlank() || instrumentIdValue.isNotBlank()) {
                "At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided in ${bean.javaClass.name}"
            }
            val instrumentId = when {
                tickerValue.isNotBlank() -> tickerValue
                instrumentIdValue.isNotBlank() -> instrumentIdValue
                else -> figiValue
            }
            check(subscriptionInterval != SubscriptionInterval.SUBSCRIPTION_INTERVAL_UNSPECIFIED) {
                "SubscriptionInterval is required for CandleHandler ${bean.javaClass.name}"
            }
            val pair = instrumentId to subscriptionInterval
            check(pair !in uniquePairs) {
                "$instrumentId with $subscriptionInterval already exist ${bean.javaClass.name}"
            }
            uniquePairs.add(pair)
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = bean
}
