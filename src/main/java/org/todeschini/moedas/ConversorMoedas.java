package org.todeschini.moedas;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.todeschini.exception.ConverterMoedaException;


import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Map;

import static java.text.MessageFormat.format;

@ApplicationScoped
@Slf4j
public class ConversorMoedas {


    public JsonObject cotacaoDolarReal() {
        return this.converter("USD", "BRL");
    }

    public JsonObject cotacaoEuroReal() {
        return this.converter("EUR","BRL");
    }


    public JsonObject converter(String de, String para) {
        var url = format("https://economia.awesomeapi.com.br/last/{0}-{1}", de, para);
        log.info(url);

        var moedas = new String[] {de, para};
        var map = getMaptipoMoedas();

        var achou = Arrays.stream(moedas).filter(
                moeda -> !map.containsKey(moeda)
        ).findFirst();

        if (achou.isPresent()) {
            var msg = format("A modeda {0} nao esta presende dentro das possiveis", achou.get());
            log.error(msg);
            throw new ConverterMoedaException(msg);
        }

        try {

            var json = new JsonObject(callRestApi(url));
            log.info(json.toString());

            var sub = de + para;

            return json.getJsonObject(sub);
            //System.out.println(" valor de compra " + json.getString("name") + " = " + json.getString("bid"));
            // System.out.println("valor de venda " + json.getString("name") + " = " +  json.getString("ask"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ConverterMoedaException(e);
        }
    }

    private String callRestApi(String url) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (Exception e) {
            throw new ConverterMoedaException(format("Erro ao chamar a url {0}", url));
        }
    }

    public Map<String,Object> getMaptipoMoedas() {

        try {
            var json = new JsonObject(callRestApi("https://economia.awesomeapi.com.br/json/available/uniq"));
            log.info(json.toString());
            //map.entrySet().stream().forEach(entry -> System.out.println(entry.getKey() + " - " + entry.getValue()));
            return json.getMap();

        } catch (Exception e) {
            throw new ConverterMoedaException("Erro ao chamar a busca por mapa de tipo de moedas");
        }
    }

    public static void main(String[] args) {
        var c = new ConversorMoedas();
        //c.converter("USD-BRL");
        System.out.println(c.cotacaoDolarReal());
        System.out.println(c.cotacaoEuroReal());
        System.out.println(c.getMaptipoMoedas());
        c.converter("XYZ", "BLA");
    }
}
