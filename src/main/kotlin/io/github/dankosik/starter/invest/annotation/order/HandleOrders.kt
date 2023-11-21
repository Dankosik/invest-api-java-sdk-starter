package io.github.dankosik.starter.invest.annotation.order

import io.github.dankosik.starter.invest.contract.orders.BaseOrdersHandler
import org.springframework.stereotype.Service
import ru.tinkoff.piapi.contract.v1.InstrumentType

@Service
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleOrders(
    val account: String = "",
    val ticker: String = "",
    val figi: String = "",
    val instrumentUid: String = "",
    val sandboxOnly: Boolean = false,
    val instrumentType: InstrumentType = InstrumentType.INSTRUMENT_TYPE_UNSPECIFIED
)

fun List<BaseOrdersHandler>.extractTickersWithoutInstrumentType() =
    this.map { it.javaClass.getAnnotation(HandleOrders::class.java) }
        .filter { it.instrumentType == InstrumentType.INSTRUMENT_TYPE_UNSPECIFIED }
        .map { it.ticker }

fun List<BaseOrdersHandler>.extractTickerToInstrumentTypeMap() =
    this.map { it.javaClass.getAnnotation(HandleOrders::class.java) }
        .filter { it.ticker.isNotBlank() }
        .filter { it.instrumentType != InstrumentType.INSTRUMENT_TYPE_UNSPECIFIED }
        .associateBy(keySelector = { it.ticker }, valueTransform = { it.instrumentType })

fun List<BaseOrdersHandler>.extractFigiToInstrumentTypeMap() =
    this.map { it.javaClass.getAnnotation(HandleOrders::class.java) }
        .filter { it.figi.isNotBlank() }
        .filter { it.instrumentType != InstrumentType.INSTRUMENT_TYPE_UNSPECIFIED }
        .associateBy(keySelector = { it.figi }, valueTransform = { it.instrumentType })

fun List<BaseOrdersHandler>.extractUidToInstrumentTypeMap() =
    this.map { it.javaClass.getAnnotation(HandleOrders::class.java) }
        .filter { it.instrumentUid.isNotBlank() }
        .filter { it.instrumentType != InstrumentType.INSTRUMENT_TYPE_UNSPECIFIED }
        .associateBy(keySelector = { it.instrumentUid }, valueTransform = { it.instrumentType })