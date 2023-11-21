package io.github.dankosik.starter.invest.contract.portfolio

import ru.tinkoff.piapi.contract.v1.PortfolioResponse

interface BlockingPortfolioHandler : BasePortfolioHandler {
    fun handleBlocking(portfolioResponse: PortfolioResponse)
}