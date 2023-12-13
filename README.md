# invest-api-java-sdk-starter

Пока в работе - следите за обновлениями, если есть вопросы пишите https://t.me/KorytoDaniil

### Небольшое фича превью:
Писать можно будет на java/kotlin (другие jvm не тестил) + spring boot

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

Помимо использования интерфейса `CoroutineCandleHandler` есть еще `BlockingCandleHandler` и `AsyncCandleHandler` используйте то что вам удобно.

Если у вас `jdk 21+` то все ваши блокирующие хендеры будут запущены в виртульных потоках, поэтому смело используйте блокирующий код без проблем 

`LastPrice`, `Trade`, `OrderBook`, `Porfolio` и остальные события доступные в стримах invest-api-java-sdk можно будет обрабатывать также как и в примере выше используя другие аннотации и интерфейсы

Подробные гайды, статьи и примеры скоро появятся - ждите
### Добавить зависимость в свой проект

Для build.`gradle.kts`
```gradle
implementation("io.github.dankosik:invest-api-java-sdk-starter:0.6.1-beta32")
```
Для `build.gradle`
```gradle
implementation 'io.github.dankosik:invest-api-java-sdk-starter:0.6.1-beta32'
```
Для maven
```asciidoc
<dependency>
    <groupId>io.github.dankosik</groupId>
    <artifactId>invest-api-java-sdk-starter</artifactId>
    <version>0.6.1-beta32</version>
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