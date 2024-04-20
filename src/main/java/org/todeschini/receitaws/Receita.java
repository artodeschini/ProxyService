package org.todeschini.receitaws;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.todeschini.exception.ReceitaException;
import org.todeschini.utils.HTTPS;

import javax.enterprise.context.ApplicationScoped;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static java.text.MessageFormat.format;

@ApplicationScoped
@Slf4j
public class Receita {

//    Esta API permite 3 consultas por minuto. No caso do limite ser excedido, o código HTTP retornado é o 429.
    private static final String URL_RECEITA_WS = "https://receitaws.com.br/v1/cnpj/{0}";

    private static final int STATUS_OK = 200;
    private static final int STATUS_WAIT = 429;
    private static final String STATUS = "status";
    private static final String CONTENT = "content";

    private JsonObject interno(String cnpj) {
        var retorno = new JsonObject();

        cnpj = cnpj.replaceAll("[^\\d.]", "");
        try {
            HTTPS.k();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new ReceitaException("erro ao desconsiderando https k no ws de receitas", e);
        }

        var html = new StringBuilder();
        var status = 0;

        try {

            var uri = format(URL_RECEITA_WS, cnpj);
            var url = new URL(uri);
            var connection = (HttpsURLConnection) url.openConnection();
            status = connection.getResponseCode();

            if ( status == 200) {
                var reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String line = reader.readLine();

                while (line != null) {
                    html.append(line);
                    line = reader.readLine();
                }
            }

        } catch (IOException e) {
            log.error("erro ao ler dados da API Receitas");
            throw new ReceitaException("erro ao ler dados da API Receitas ", e);
        }

        retorno.put(STATUS, status);
        retorno.put(CONTENT, new JsonObject(html.toString()));

        return retorno;
    }

    public JsonObject call(String cnpj) {
        var chamada = this.interno(cnpj);

        if (chamada.getInteger(STATUS) == STATUS_OK) {
            return chamada.getJsonObject(CONTENT);

        } else {
            try {
                TimeUnit.MINUTES.sleep(2);
            } catch (InterruptedException e) {
                throw new ReceitaException("erro ao esperar receitas WS", e);
            }
            return this.call(cnpj);
        }
    }
}
