package org.todeschini.fenaban;

import io.vertx.core.json.JsonArray;
import org.todeschini.dto.FeriadoFenaban;
import org.todeschini.exception.FenanbanException;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

@ApplicationScoped
public class Fenaban {

    private static final Logger LOGGER = Logger.getLogger(Fenaban.class.getName());

    private static final String URL_FERIADOS_BANCARIOS =
            //https://feriadosbancarios.febraban.org.br/Home/ObterFeriadosFederaisF?ano=2022 traz quarta de cinza e dia 31
            "https://feriadosbancarios.febraban.org.br/Home/ObterFeriadosFederais?ano={0}";

    private int getMonthAsInt(String date) {
        if (date.toLowerCase().contains("janeiro")) {
            return 1;
        } else if (date.toLowerCase().contains("fevereiro")) {
            return 2;
        } else if (date.toLowerCase().contains("março")) {
            return 3;
        } else if (date.toLowerCase().contains("mar")) {
            return 3;
        } else if (date.toLowerCase().contains("abril")) {
            return 4;
        } else if (date.toLowerCase().contains("maio")) {
            return 5;
        } else if (date.toLowerCase().contains("junho")) {
            return 6;
        } else if (date.toLowerCase().contains("julho")) {
            return 7;
        } else if (date.toLowerCase().contains("agosto")) {
            return 8;
        } else if (date.toLowerCase().contains("setembro")) {
            return 9;
        } else if (date.toLowerCase().contains("outubro")) {
            return 10;
        } else if (date.toLowerCase().contains("novembro")) {
            return 11;
        } else if (date.toLowerCase().contains("dezembro")) {
            return 12;
        } else {
            throw new FenanbanException("Erro ao obter o mes como numerico na data como string: ".concat(date));
        }
    }

    public List<FeriadoFenaban> feriadosBancariosByAno(int ano) {
        var feriados = new ArrayList<FeriadoFenaban>();
        var array = new JsonArray(this.openFeriadosBancarios(ano));

        String diaMes;

        for (int i = 0; i < array.size(); i++) {
            var json = array.getJsonObject(i);
            var f = new FeriadoFenaban();
            diaMes = json.getString("diaMes");
            f.setDate(
                    LocalDate.of(ano, getMonthAsInt(diaMes),
                            Integer.parseInt(diaMes.replaceAll("[^\\d.]", ""))));
            f.setDescricao(json.getString("nomeFeriado"));
            f.setDiaSemana(json.getString("diaSemana"));

            feriados.add(f);
        }

        return feriados;
    }

    private String openFeriadosBancarios(int ano) {
        try {
            HTTPS.k();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new FenanbanException("erro ao ler dados da Fenaban desconsiderando https k", e);
        }

        var json = new StringBuilder();

        try {
            var uri = format(URL_FERIADOS_BANCARIOS, String.valueOf(ano).replaceAll("[^\\d.]", ""));
            LOGGER.info(format("lendo informacoes da url {0}", uri));
            var url = new URL(uri);
            var connection = (HttpsURLConnection) url.openConnection();

            var reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

            String line = reader.readLine();
            while (line != null) {
                json.append(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            LOGGER.severe("erro ao ler os dados da pagina do FENABAN");
            throw new FenanbanException("erro ao ler os dados da pagina do FENABAN", e);
        }

        return json.toString();
    }

//    public static void main(String[] args) {
//        Fenaban f = new Fenaban();
//        System.out.println(f.feriadosBancariosByAno(2022));
////        String s2 = new String(f.openFeriadosBancarios(2022), "UTF-8");
//
//    }
}
