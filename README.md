# invest-api-java-sdk-starter

Пока в работе - следите за обновлениями, если есть вопросы пишите https://t.me/KorytoDaniil

### Небольшое фича превью:
Писать можно будет на java/kotlin (другие jvm не тестил) + spring boot. Для использования необходима jdk17+.

Ниже пример обработки минутных свечей по Фьючерсу на доллар
```kotlin
@HandleCandle(
    ticker = "SiZ3",
    subscriptionInterval = SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
)
class DollarCandleHandler : CoroutineCandleHandler {

    override suspend fun handle(candle: Candle) {
        println("DollarCandleHandler $candle")
    }
}
```

Помимо использования интерфейса `CoroutineCandleHandler` есть еще `BlockingCandleHandler` и `AsyncCandleHandler` используйте то что вам удобно. Вместо тикера можно использовать figi или instrumentUid

Если у вас `jdk 21+` то все ваши блокирующие хендеры будут запущены в виртульных потоках, поэтому смело используйте блокирующий код без проблем 

`LastPrice`, `Trade`, `OrderBook`, `Porfolio` и остальные события доступные в стримах invest-api-java-sdk можно будет обрабатывать также как и в примере выше используя другие аннотации и интерфейсы

Также добавьте в aplication.yml

```yml
tinkoff:
  starter:
    apiToken:
      fullAccess:
        "ваш токен"
```

Вместо `fullAccess` можно использовать `readonly` или `sandbox`. Все ваши запросы к api будут использовать определенный вами токен. 

Все сервисы api, такие как: `MarketDataService`, `InstrumentsService`, `OrdersService` и т.д.  будут созданы как компоненты spring. Поэтому вы можете использвать их в ваших хендлерах например вот так:

```kotlin
@HandleCandle(
    ticker = "SiZ3",
    subscriptionInterval = SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
)
class DollarCandleHandler(
    private val marketDataService: MarketDataService,
    private val ordersService: OrdersService,
    private val instrumentsService: InstrumentsService,
) : CoroutineCandleHandler {

    override suspend fun handle(candle: Candle) {
        val uid = instrumentsService.getFutures(InstrumentStatus.INSTRUMENT_STATUS_BASE)
            .awaitSingle()
            .find { it.ticker == "ваш тикер" }?.uid
        marketDataService.getOrderBook(uid!!, 50)
        ordersService.getOrders("id вашего аккаунта")
    }
}
```

Подробные гайды, статьи и примеры скоро появятся - ждите
### Добавить зависимость в свой проект

Для build.`gradle.kts`
```gradle
implementation("io.github.dankosik:invest-api-java-sdk-starter:0.6.1-beta33")
```
Для `build.gradle`
```gradle
implementation 'io.github.dankosik:invest-api-java-sdk-starter:0.6.1-beta33'
```
Для maven
```asciidoc
<dependency>
    <groupId>io.github.dankosik</groupId>
    <artifactId>invest-api-java-sdk-starter</artifactId>
    <version>0.6.1-beta33</version>
    <type>pom</type>
</dependency>
```

Также необходимо добавить зависимость 

```
implementation("org.springframework.boot:spring-boot-starter-web")
```
Или
```asciidoc
implementation("org.springframework.boot:spring-boot-starter-webflux")
```

### Pull Requests

На все ваши пулл реквесты будет приходить chatGPT, не пугайтесь, возможно он посоветует что-то хорошее  или найдет очепятку)