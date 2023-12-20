package com.fshahy.catalog.services;

import java.util.logging.Logger;

import io.helidon.common.context.Context;
import io.helidon.common.context.Contexts;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbExecute;
import io.helidon.dbclient.DbTransaction;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public class ProductCategoryService implements HttpService {

    private final System.Logger LOGGER = System.getLogger(ProductCategoryService.class.getName());
    private final DbClient dbClient;
    private final boolean initSchema;
    private final boolean initData;

    public ProductCategoryService() {
        Config config = Config.global().get("db");
        this.dbClient = Contexts.globalContext()
                                .get(DbClient.class)
                                .orElseGet(() -> DbClient.create(config));

        initSchema = config.get("init-schema").asBoolean().orElse(false);
        initData = config.get("init-data").asBoolean().orElse(false);
        init();
    }

    private void init() {
        if (initSchema) {
            initSchema();
        }

        if (initData) {
            initData();
        }
    }

    private void initSchema() {
        DbExecute exec = dbClient.execute();

        try {
            exec.namedDml("create-table-product-categories");
        } catch(Exception ex) {
            LOGGER.log(System.Logger.Level.ERROR, "could not create table: product_categories", ex);
        }
    }

    private void initData() {
        DbTransaction tx = dbClient.transaction();
        try {
            tx.namedInsert("insert-product-category", 1, "Categ 1");
            tx.commit();
        } catch(Throwable t) {
            tx.rollback();
            throw t;
        }
    }

    @Override
    public void routing(HttpRules rules) {
        rules.get("/", this::listCategories);
    }
    
    private void listCategories(ServerRequest request, ServerResponse response) {
        response.send("");
    }
}
