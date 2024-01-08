package io.github.dankosik.starter.invest.contract.operation.portfolio

import ru.tinkoff.piapi.contract.v1.PortfolioResponse
import java.util.concurrent.CompletableFuture

interface AsyncPortfolioHandler : BasePortfolioHandler {
    fun handleAsync(portfolioResponse: PortfolioResponse): CompletableFuture<Void>
}