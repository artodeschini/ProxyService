# Proxy Service

## Requires

* Java 11
* Maven 3.6 >
* jsoup
* quarkus
* graalvm (opctional executar como nativo)
* Docker

## to build

```bash
mvn clean install
```

## to run application local

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

## to run application with docker

```bash
mvn clean install

# cria a imagem
docker build -t proxy-service .

# executa a applicacao de iterativa
docker run -it --rm --name proxy-teste -p 8080:8080 proxy-service

# executa como demon para vps
docker run -d --rm --name proxy-teste -p 8080:8080 proxy-service
```

## Notas da versao 2.0.2

* Ajustes para separar o endereco do complemento

## Notas da versao 2.0.1

* WSDL do correio exige senha alterado para fazer crawler do site do correios

## Notas da versao 2.0

* Aplicaçao migrada para utilizar microsrviço com quarkus
* Adicionado crawler do IBGE para encontrar o código da cidade 

## notas da versao 1.0.0
* utilizava wso2 esb para fazer o proxy
* Este era um projeto de um class mediator para WSO2 ESB