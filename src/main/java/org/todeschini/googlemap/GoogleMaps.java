package org.todeschini.googlemap;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import org.todeschini.correios.ProxyWebServiceCorreios;
import org.todeschini.dto.EnderecoBuscaGoogle;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
@Slf4j
public class GoogleMaps {

    @Inject
    private ProxyWebServiceCorreios correios;

    private static final double AVERAGE_RADIUS_OF_EARTH = 6371;

    private static final String GOOGLE_API_KEY = System.getenv("GOOGLE_API_KEY");

    private static final String SEPARADOR = ", ";
    private static final String PROTOCOLO_HTTPS = "https://";

    private String carregaGoogleApiKey() {
        if (GOOGLE_API_KEY != null && !GOOGLE_API_KEY.isEmpty()) {
            return "&key=" + GOOGLE_API_KEY;
        }
        return "";
    }

    public String calcularDistancia(EnderecoBuscaGoogle origem, EnderecoBuscaGoogle destino) {
        var inicio = new StringBuilder()
                .append(origem.getCep())
                .append(SEPARADOR)
                .append(origem.getCidade())
                .append(SEPARADOR)
                .append(origem.getBairro())
                .append(SEPARADOR)
                .append(origem.getEnd())
                .append(SEPARADOR)
                .append(origem.getNumero());

        var fim = new StringBuilder()
                .append(destino.getCep())
                .append(SEPARADOR)
                .append(destino.getCidade())
                .append(SEPARADOR)
                .append(destino.getBairro())
                .append(SEPARADOR)
                .append(destino.getEnd())
                .append(SEPARADOR)
                .append(destino.getNumero());

        try {
            var urlDirections = new StringBuilder()
                    .append(PROTOCOLO_HTTPS)
                    .append("maps.google.com/maps/api/directions/json?origin=")
                    .append(inicio.toString())
                    .append("&destination=")
                    .append(fim.toString())
                    .append("&region=br&sensor=false");

            urlDirections.append(carregaGoogleApiKey());

            log.info(urlDirections.toString());


            var distancia = getDistanceGoogleDirections(urlDirections.toString());

            if (distancia == null) {

                var destinoSemCep = new StringBuilder()
                        .append(destino.getEnd())
                        .append(SEPARADOR)
                        .append(destino.getNumero())
                        .append(SEPARADOR)
                        .append(destino.getBairro())
                        .append(SEPARADOR)
                        .append(destino.getCidade());

                urlDirections = new StringBuilder();

                urlDirections.append(PROTOCOLO_HTTPS)
                        .append("maps.google.com/maps/api/directions/json?origin=")
                        .append(origem.toString())
                        .append("&destination=")
                        .append(destinoSemCep.toString())
                        .append("&region=br&sensor=false");

                urlDirections.append(carregaGoogleApiKey());

                log.info(urlDirections.toString());

                return getDistanceGoogleDirections(urlDirections.toString());

            } else {
                return distancia;
            }

        } catch (final Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }

    /**
     * @return Distancia em KM localizada no xml
     * @throws Exception .
     */
    private String getDistanceGoogleDirections(String urlDirections) throws Exception {
        var url = new URL(urlDirections.replace(" ", "%20"));
        var document = callUrl(url);

        return parseJsonToDistance(document);
    }

    private String callGoogleDirections(String urlDirections) throws Exception {
        var url = new URL(urlDirections.replace(" ", "%20"));
        return callUrl(url);

        //return parseJsonToDistance(document);
    }

    private String parseJsonToDistance(final String document) {
        // | jq . | jq .routes | jq '.[0]' | jq .legs | jq '.[0]' | jq .distance
        var doc = new JsonObject(document);
        log.info(doc.toString());
        var routes = doc.getJsonArray("routes");
        if (routes.size() > 0 ) {
            var legs = routes.getJsonObject(0).getJsonArray("legs");
            if (legs.size() > 0 ) {
                var distance = legs.getJsonObject(0).getJsonObject("distance");
                return distance.getString("text");
            }
        }

        return null;
    }

    public String callUrl(final URL url) {
        var json = new StringBuilder();
        try {

            var conn = (HttpsURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "text/json; charset=utf-8");
            conn.setRequestProperty("Content-Type", "application/json");

            var buffer = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line = buffer.readLine();
            while (line != null) {
                json.append(line);
                line = buffer.readLine();
            }

        } catch (Exception e) {
            log.debug("Erro ao consultar api do google ".concat(e.getMessage()), e);
        }

        return json.toString();
    }

    public JsonObject calcularDistancia(String strCepOrigemStr, int numeroOrigem, String strCepDestino, int numeroDestino) {
        var cepOrigem = correios.crawlerWebSiteCorreios(strCepOrigemStr);
        var cepDestino = correios.crawlerWebSiteCorreios(strCepDestino);

        var origem = EnderecoBuscaGoogle.toEnderecoCepToEnderecoBuscaGoogle(cepOrigem, String.valueOf(numeroOrigem));
        var destino = EnderecoBuscaGoogle.toEnderecoCepToEnderecoBuscaGoogle(cepDestino, String.valueOf(numeroDestino));

        var json = new JsonObject();
        json.put("origem", origem.toJsonObject());
        json.put("destino", destino.toJsonObject());
        json.put("distancia", calcularDistancia(origem, destino));

        return json;
    }


//    public static void main(String[] args) throws Exception {
//        //var googleURL = "https://maps.google.com/maps/api/directions/json?origin=88015-190, Florianopolis - SC, Centro, Avenida Aycar Saddi, 190&destination=88090-351, Florianopolis - SC, Coloninha, Rua Santa Rita de Cassia, 123&region=br&sensor=false&key=
//
//
//        var google = new GoogleMaps();
//        //google.getDistanceGoogleDirections(googleURL);
//        //google.getJson(googleURL);
//        //google.getDocumento(new URL(googleURL));
//        //google.findGeocodeInformation("Rua Santa Rita de Cassia, 829 Florianopolis Santa Catarina", "teste");
//       //var rest = google.calcularDistancia("88015-190", 190, "88090-351", 123);
//        var rest = google.calcularDistancia("88090350", 829, "90420121", 914);
//        System.out.println(rest);
//    }

}
