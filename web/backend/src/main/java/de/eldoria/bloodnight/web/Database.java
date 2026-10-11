/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.postgresql.databases.PostgreSql;
import de.chojo.sadu.postgresql.mapper.PostgresqlMapper;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.updater.QueryReplacement;
import de.chojo.sadu.updater.SqlUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Connects to PostgreSQL, migrates the schema and installs the default query configuration.
 */
final class Database {
    private static final Logger log = LoggerFactory.getLogger(Database.class);

    private Database() {}

    /**
     * @param config the connection
     * @return the pool, migrated
     */
    static DataSource open(ServiceConfig.DatabaseConfig config) throws SQLException, IOException {
        DataSource source = DataSourceCreator.create(PostgreSql.get())
                .configure(builder -> builder.host(config.host())
                        .port(config.port())
                        .database(config.name())
                        .user(config.user())
                        .password(config.password())
                        .currentSchema(config.schema())
                        .applicationName("BloodNight Web"))
                .create()
                .withMaximumPoolSize(5)
                .withMinimumIdle(1)
                .build();
        SqlUpdater.builder(source, PostgreSql.get())
                .setReplacements(new QueryReplacement("web_schema", config.schema()))
                .setSchemas(config.schema())
                .execute();
        QueryConfiguration.setDefault(QueryConfiguration.builder(source)
                .setExceptionHandler(error -> log.error("Database query failed", error))
                .setThrowExceptions(true)
                .setRowMapperRegistry(new RowMapperRegistry().register(PostgresqlMapper.getDefaultMapper()))
                .build());
        return source;
    }
}
