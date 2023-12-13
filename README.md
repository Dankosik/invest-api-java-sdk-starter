# invest-api-java-sdk-starter

Пока в работе - следите за обновлениями, если есть вопросы пишите https://t.me/KorytoDaniil

Добавить зависимость в свой проект

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