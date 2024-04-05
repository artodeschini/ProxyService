package org.todeschini;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import lombok.extern.slf4j.Slf4j;

import java.util.logging.Logger;

@QuarkusMain
@Slf4j
public class App {

    private static Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String... args) {
        log.info("Start App Proxy para cep e ibge");
        Quarkus.run(args);
    }
}
