
package com.fshahy.catalog;


import io.helidon.logging.common.LogConfig;
import io.helidon.config.Config;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.observe.ObserveFeature;
import io.helidon.webserver.observe.health.HealthObserver;
import io.helidon.dbclient.health.DbClientHealthCheck;

import com.fshahy.catalog.services.ProductCategoryService;

import io.helidon.common.context.Contexts;
import io.helidon.dbclient.DbClient;




/**
 * The application main class.
 */
public class Main {


    /**
     * Cannot be instantiated.
     */
    private Main() {
    }


    /**
     * Application main entry point.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        
        // load logging configuration
        LogConfig.configureRuntime();

        // initialize global config from default configuration
        Config config = Config.create();
        Config.global(config);

        
        DbClient dbClient = DbClient.create(config.get("db"));
        Contexts.globalContext().register(dbClient);

        
        ObserveFeature observe = ObserveFeature.builder()
                .config(config.get("server.features.observe"))
                .addObserver(HealthObserver.builder()
                                     .addCheck(DbClientHealthCheck.create(dbClient, config.get("db.health-check")))
                                     .build())
                .build();


        WebServer server = WebServer.builder()
                .config(config.get("server"))
                .addFeature(observe)
                .routing(Main::routing)
                .build()
                .start();


        System.out.println("WEB server is up! http://localhost:" + server.port() + "/simple-greet");

    }


    /**
     * Updates HTTP Routing.
     */
    static void routing(HttpRouting.Builder routing) {
        routing
                .register("/categories", new ProductCategoryService());
    }
}