package org.todeschini.correios;

import io.vertx.core.json.JsonObject;
import org.todeschini.dto.EnderecoCep;
import org.todeschini.dto.MuniciopioIbge;
import org.todeschini.exception.CorreiosServiceException;
import org.todeschini.ibge.IbgeCrawler;
import org.todeschini.utils.HTTPS;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Optional;

@ApplicationScoped
public class ProxyCorreiosNovo {

    @Inject
    private IbgeCrawler crawler; // = new IbgeCrawler();

    public static void main(String[] args) {
        try {
            System.out.println(new ProxyCorreiosNovo().novo("88090350"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public EnderecoCep novo(String cep) throws IOException, InterruptedException {
        // convertido do curl para java no site abaixo
        /// https://curlconverter.com/java/

        var body = new StringBuilder("pagina=%2Fapp%2Fendereco%2Findex.php&cepaux=&mensagem_alerta=&endereco=")
        .append(cep).append("&tipoCEP=LOG");

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://buscacepinter.correios.com.br/app/endereco/carrega-cep-endereco.php"))
                //.POST(BodyPublishers.ofString("pagina=%2Fapp%2Fendereco%2Findex.php&cepaux=&mensagem_alerta=&endereco=88090350&tipoCEP=LOG"))
                .POST(BodyPublishers.ofString(body.toString()))
                .setHeader("accept", "*/*")
                .setHeader("accept-language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                .setHeader("cache-control", "no-store, no-cache, must-revalidate")
                .setHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                //.setHeader("cookie", "_ga=GA1.3.887944617.1711682292; _gid=GA1.3.1273195035.1711682296; rxVisitor=1711682303310C7S7RSSKJE9N3NPBA66601MEG2LO8EUV; rxvt=1711684103323|1711682303310; dtCookie=v_4_srv_1_sn_RBS8EN9H569H0T1HBAHDJJ5QM2I5EHVL_app-3A47fca5cf7bd2003c_1_ol_0_perc_100000_mul_1; dtSa=true%7CC%7C-1%7CBusca%20CEP%20ou%20Endere%C3%A7o%7C-%7C1711682353752%7C482303309_298%7Chttps%3A%2F%2Fwww.correios.com.br%2F%7C%7C%7C%7C; INGRESSCOOKIE=1711682355.215.10718.823969|a18ecd9549ddde51e3f47ee40455f85d; buscacep=v7fhph7naq8tk60cjl9n4t54fl; LBprdint2=1614413834.47873.0000; LBprdExt1=701038602.47873.0000; _ga_J59GSF3WW5=GS1.1.1711682291.1.1.1711682353.60.0.0; dtPC=1$482303309_298h-vVMEHHARALKMPSEDPIMEFGLPMFWNNHOSR-0e0")
                .setHeader("origin", "https://buscacepinter.correios.com.br")
                .setHeader("referer", "https://buscacepinter.correios.com.br/app/endereco/index.php")
                //.setHeader("sec-ch-ua", "\"Google Chrome\";v=\"123\", \"Not:A-Brand\";v=\"8\", \"Chromium\";v=\"123\"")
                //.setHeader("sec-ch-ua-mobile", "?0")
                //.setHeader("sec-ch-ua-platform", "\"macOS\"")
                //.setHeader("sec-fetch-dest", "empty")
                //.setHeader("sec-fetch-mode", "cors")
                //.setHeader("sec-fetch-site", "same-origin")
                //.setHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());

        var endereco = new EnderecoCep();

        var json = new JsonObject(response.body().toString());
        if (!json.getBoolean("erro")) {
            var array = json.getJsonArray("dados");
            var dados = array.getJsonObject(0);
            endereco = EnderecoCep.builder()
                    .uf(dados.getString("uf"))
                    .cidade(dados.getString("localidade"))
                    .end(dados.getString("logradouroDNEC"))
                    .bairro(dados.getString("bairro"))
                    .cep(dados.getString("cep"))
                    .build();

            Optional<MuniciopioIbge> municiopioIbge = crawler.findMunicipioIbge(endereco.getUf(), endereco.getCidade());

            if (municiopioIbge.isPresent()) {
                endereco.setIbge(municiopioIbge.get().getCodigo());
            }
        }

        return endereco;
    }

}
