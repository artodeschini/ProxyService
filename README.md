# Proxy Service

## Requires

* Java 11
* Maven 3.6 >
* jsoup
* quarkus
* graalvm (opctional executar como nativo)

## to build

```bash
mvn clean install
```

## to run application

```bash
java -jar target/quarkus-app/quarkus-run.jar
```


## Notas da versao 2.0

* Aplicaçao migrada para utilizar microsrviço com quarkus
* Adicionado crawler do IBGE para encontrar o código da cidade 

## notas da versao 1.0.0
* utilizava wso2 esb para fazer o proxy
* Este era um projeto de um class mediator para WSO2 ESB