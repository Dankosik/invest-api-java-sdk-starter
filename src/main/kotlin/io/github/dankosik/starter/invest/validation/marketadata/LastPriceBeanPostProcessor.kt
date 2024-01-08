package io.github.dankosik.starter.invest.validation.marketadata

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllLastPrices
import io.github.dankosik.starter.invest.annotation.marketdata.HandleLastPrice
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.AsyncLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.BlockingLastPriceHandler
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.CoroutineLastPriceHandler
import org.springframework.beans.factory.config.BeanPostProcessor

internal class LastPriceBeanPostProcessor : BeanPostProcessor {


    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val isHandleLastPrice = bean.javaClass.declaredAnnotations.filterIsInstance<HandleLastPrice>().isNotEmpty()
        val isAllHandleLastPrice =
            bean.javaClass.declaredAnnotations.filterIsInstance<HandleAllLastPrices>().isNotEmpty()
        val javaClassName = bean.javaClass.name
        check(
            !(isHandleLastPrice
                    && (bean !is CoroutineLastPriceHandler && bean !is BlockingLastPriceHandler && bean !is AsyncLastPriceHandler))
        ) { "Class: $javaClassName annotated with HandleLastPrice should implement AsyncLastPriceHandler or BlockingLastPriceHandler or CoroutineLastPriceHandler" }
        check(
            !(isAllHandleLastPrice
                    && (bean !is CoroutineLastPriceHandler && bean !is BlockingLastPriceHandler && bean !is AsyncLastPriceHandler))
        ) { "Class: $javaClassName annotated with HandleAllLastPrices should implement AsyncLastPriceHandler or BlockingLastPriceHandler or CoroutineLastPriceHandler" }
        if (isHandleLastPrice) {
            val classNameInMessage = when (bean) {
                is CoroutineLastPriceHandler -> "CoroutineLastPriceHandler"
                is BlockingLastPriceHandler -> "BlockingLastPriceHandler"
                else -> "AsyncLastPriceHandler"
            }
            check(isHandleLastPrice) {
                "$classNameInMessage: $javaClassName must have an annotated of HandleLastPrice"
            }
            val annotation = bean.javaClass.getAnnotation(HandleLastPrice::class.java)
            val tickerValue = annotation.ticker
            val figiValue = annotation.figi
            val instrumentIdValue = annotation.instrumentUid
            check(tickerValue.isNotBlank() || figiValue.isNotBlank() || instrumentIdValue.isNotBlank()) {
                "At least one of the arguments 'ticker', 'figi' or 'instrumentId' must be provided in $javaClassName"
            }
        }

        if (isAllHandleLastPrice) {
            val annotation = bean.javaClass.getAnnotation(HandleAllLastPrices::class.java)
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
