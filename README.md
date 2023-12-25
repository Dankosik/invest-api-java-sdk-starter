# Spring Boot стартер для [invest-api-java-sdk](https://github.com/RussianInvestments/invest-api-java-sdk)
### Простота написания кода + высокая производительность из коробки

Пока в работе - следите за обновлениями, если есть вопросы пишите https://t.me/KorytoDaniil

### Пререквизиты
- `jdk17+` 
- `Maven 3+` либо `Gradle 8.5+` (если `jdk21+`) и `Gradle 7.3+` (если `jdk17+`)
- Также добавьте в `aplication.yml`

```yml
tinkoff:
  starter:
    apiToken:
      fullAccess:
        "ваш токен"
```
Вместо `fullAccess` можно использовать `readonly` или `sandbox`. Все ваши запросы к api будут использовать определенный вами токен.
### Возможности:

Три способа обработать минутные свечи по Фьючерсу на доллар:
```java
@HandleCandle(
        ticker = "SiH4",
        subscriptionInterval = SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
)
class DollarCandleHandler implements AsyncCandleHandler {

    @Override
    public CompletableFuture<Void> handleAsync(Candle candle) {
        return CompletableFuture.runAsync(() -> System.out.println(candle));
    }
}
```
```java
@HandleCandle(
        ticker = "SiH4",
        subscriptionInterval = SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
)
class BlockingDollarCandleHandler implements BlockingCandleHandler {

    @Override
    public void handleBlocking(Candle candle) {
        System.out.println(candle);
    }
}
```
Для kotlin
```kotlin
@HandleCandle(
    ticker = "SiH4",
    subscriptionInterval = SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
)
class DollarCandleHandler : CoroutineCandleHandler {

    override suspend fun handle(candle: Candle) {
        println("DollarCandleHandler $candle")
    }
}
```
Вместо тикера можно использовать `figi` или `instrumentUid`

Если у вас `jdk 21+` то все ваши `BlockingCandleHandler` будут запущены в виртульных потоках, поэтому смело используйте блокирующий код без проблем 

`LastPrice`, `Trade`, `OrderBook`, `Porfolio` и остальные события доступные в стримах `invest-api-java-sdk` можно будет обрабатывать также как и в примерах выше используя другие аннотации и интерфейсы. Подробнее можно посмотреть примеры:

### Примеры
[На kotlin + gradle.kts](
https://github.com/Dankosik/invest-starter-demo/blob/main/src/main/kotlin/io/github/dankosik/investstarterdemo/InvestStarterDemoApplication.kt#L65) <br>
[На java + maven](
https://github.com/Dankosik/invest-starter-demo-java/blob/main/src/main/java/io/github/dankosik/investstarterdemojava/InvestStarterDemoJavaApplication.java#L44)

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

Подробные гайды и статьи скоро появятся - ждите

### Добавить зависимость в свой проект

Для `build.gradle.kts`
```gradle
implementation("io.github.dankosik:invest-api-java-sdk-starter:0.6.1-beta40")
```
Для `build.gradle`
```gradle
implementation 'io.github.dankosik:invest-api-java-sdk-starter:0.6.1-beta40'
```
Для `maven`
```asciidoc
<dependency>
    <groupId>io.github.dankosik</groupId>
    <artifactId>invest-api-java-sdk-starter</artifactId>
    <version>0.6.1-beta40</version>
    <classifier>plain</classifier>
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