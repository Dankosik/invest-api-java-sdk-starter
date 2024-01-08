package io.github.dankosik.starter.invest.annotation.marketdata

import io.github.dankosik.starter.invest.contract.marketdata.candle.BaseCandleHandler
import org.springframework.stereotype.Service
import ru.tinkoff.piapi.contract.v1.InstrumentType
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleCandle(
    val ticker: String = "",
    val figi: String = "",
    val instrumentUid: String = "",
    val sandboxOnly: Boolean = false,
    val instrumentType: InstrumentType = InstrumentType.INSTRUMENT_TYPE_UNSPECIFIED,
    val subscriptionInterval: SubscriptionInterval = SubscriptionInterval.SUBSCRIPTION_INTERVAL_UNSPECIFIED,
    val waitClose: Boolean = false
)

fun List<BaseCandleHandler>.extractTickersWithoutInstrumentType() =
    asSequence()
        .map { it.javaClass.getAnnotation(HandleCandle::class.java) }
        .filter { it.instrumentType == InstrumentType.INSTRUMENT_TYPE_UNSPECIFIED }
        .map { it.ticker }

fun List<BaseCandleHandler>.extractTickerToInstrumentTypeMap() =
    asSequence()
        .map { it.javaClass.getAnnotation(HandleCandle::class.java) }
        .filter { it.ticker.isNotBlank() }
        .filter { it.instrumentType != InstrumentType.INSTRUMENT_TYPE_UNSPECIFIED }
        .associateBy(keySelector = { it.ticker }, valueTransform = { it.instrumentType })

fun List<BaseCandleHandler>.extractFigiToInstrumentTypeMap() =
    asSequence()
        .map { it.javaClass.getAnnotation(HandleCandle::class.java) }
        .filter { it.figi.isNotBlank() }
        .filter { it.instrumentType != InstrumentType.INSTRUMENT_TYPE_UNSPECIFIED }
        .associateBy(keySelector = { it.figi }, valueTransform = { it.instrumentType })

fun List<BaseCandleHandler>.extractUidToInstrumentTypeMap() =
    asSequence()
        .map { it.javaClass.getAnnotation(HandleCandle::class.java) }
        .filter { it.instrumentUid.isNotBlank() }
        .filter { it.instrumentType != InstrumentType.INSTRUMENT_TYPE_UNSPECIFIED }
        .associateBy(keySelector = { it.instrumentUid }, valueTransform = { it.instrumentType })