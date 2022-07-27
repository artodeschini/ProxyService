package org.todeschini;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.util.logging.Logger;

@QuarkusMain
public class App {

    private static Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String ... args) {
        LOGGER.info("Start App Proxy para cep e ibge");
        Quarkus.run(args);
    }
}
