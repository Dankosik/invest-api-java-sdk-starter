package io.github.dankosik.starter.invest.extension

import kotlinx.coroutines.reactor.awaitSingle
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.CompletableFuture

suspend fun <T> CompletableFuture<out T?>.awaitSingle(): T = toMono().awaitSingle()