package io.github.dankosik.starter.invest.contract.operation.portfolio

import ru.tinkoff.piapi.contract.v1.PortfolioResponse

interface CoroutinePortfolioHandler : BasePortfolioHandler {
    suspend fun handle(portfolioResponse: PortfolioResponse)
}