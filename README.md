# Spring Boot стартер для [invest-api-java-sdk](https://github.com/RussianInvestments/invest-api-java-sdk)
### Простота написания кода + высокая производительность из коробки. В стартере упор сделан на стриминг данных - пишите только логику обработки, а за создание стримов и выбор нужных хендлеров стартер позаботится за вас.

## Пререквизиты
- `jdk17+` 
- `Maven 3+` либо `Gradle 8.5+` (если `jdk21+`) и `Gradle 7.3+` (если `jdk17+`)
- `SpringBoot 3.0+`
- Также добавьте в `aplication.yml`

```yml
tinkoff:
  starter:
    apiToken:
      fullAccess:
        "ваш токен"
```
Вместо `fullAccess` можно использовать `readonly` или `sandbox`. Все ваши запросы к api будут использовать определенный вами токен.
## Возможности

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
Или для списка инструментов:
```java
@HandleAllCandles(
        subscriptionInterval = SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE,
        tickers = {"SiH4", "SBER"}
)
class CommonCandleHandler implements AsyncCandleHandler {

    @Override
    public CompletableFuture<Void> handleAsync(Candle candle) {
        return CompletableFuture.runAsync(() -> System.out.println("CommonCandleHandler: " + candle));
    }
}
```
Для `kotlin`:
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

Тоже самое можно сделать с помощью `@Bean` следующим способом:
```java
@Configuration
class Config {
    
    @Bean
    public BlockingCandleStreamProcessorAdapter candleStreamProcessorAdapter() {
        return CandleStreamProcessorAdapterFactory
                .withSubscriptionInterval(SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE)
                .waitClose(true)
                .withTickers(List.of("SiH4"))
                .createBlockingHandler(System.out::println);
    }
}
```
Вместо тикера можно использовать `figi` или `instrumentUid`

Если у вас `jdk 21+` то все ваши `BlockingCandleHandler` будут запущены в виртульных потоках, поэтому смело используйте блокирующий код без проблем 

Обрабатывак сделки, стаканы, последние цены, обновление портфеля и т.д. можно аналогично.
`LastPrice`, `Trade`, `OrderBook`, `Porfolio` и остальные события доступные в стримах `invest-api-java-sdk` можно будет обрабатывать также как и в примерах выше используя другие аннотации и интерфейсы. Подробнее можно посмотреть примеры:

## Примеры
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

## Добавить зависимость в свой проект

<details>
  <summary>build.gradle.kts</summary>

```groovy
implementation("io.github.dankosik:invest-api-java-sdk-starter:1.6.0-RC1")
```

Также необходимо добавить зависимость <br>

```groovy
implementation("org.springframework.boot:spring-boot-starter-web")
```
Или
```groovy
implementation("org.springframework.boot:spring-boot-starter-webflux")
```
</details>

<details>
  <summary>build.gradle</summary>

```groovy
implementation 'io.github.dankosik:invest-api-java-sdk-starter:1.6.0-RC1'
```

Также необходимо добавить зависимость <br>

```groovy
implementation 'org.springframework.boot:spring-boot-starter-web'
```
Или
```groovy
implementation 'org.springframework.boot:spring-boot-starter-webflux'
```
</details>

<details>
  <summary>Для Maven</summary>

```xml
<dependency>
    <groupId>io.github.dankosik</groupId>
    <artifactId>invest-api-java-sdk-starter</artifactId>
    <version>1.6.0-RC1</version>
    <classifier>plain</classifier>
</dependency>
```
Также необходимо добавить зависимость
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

или
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```
</details>

## О версиях
Версии будут совпадать как в самой [sdk](https://github.com/RussianInvestments/invest-api-java-sdk/tags)

Тоесть при подключении
```
implementation("io.github.dankosik:invest-api-java-sdk-starter:1.6.0")
```
Будет использоваться версия sdk 1.6

Стартер поддерживает минимальную версию skd - 1.6

При появлении новых версий skd будет подниматься мажорная версия стартера, но старые будут оставаться на поддержке

## Сообщество API Тинькофф Инвестиций

* [Основной репозиторий](https://github.com/RussianInvestments/investAPI)
* [Telegram-канал](https://t.me/tinkoffinvestopenapi)
* [Telegram-чат по общим вопросам](https://t.me/joinchat/VaW05CDzcSdsPULM)
* [Telegram-чат для заказчиков и разработчиков торговых роботов](https://t.me/tinkoff_invest_robot_development)
