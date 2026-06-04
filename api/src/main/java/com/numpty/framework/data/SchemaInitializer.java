package com.numpty.framework.data;

import com.numpty.framework.exception.BootstrapException;
import com.numpty.framework.exception.CoreException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

    private SchemaInitializer() {
        throw new CoreException("Instantiation not allowed for this class.");
    }

    public static void executeSchema(DatabaseProvider dbProvider, String filename) {
        try (InputStream inputStream = SchemaInitializer.class.getClassLoader()
            .getResourceAsStream(filename)) {
            if (inputStream == null) {
                log.warn("Error occurred while reading schema file: {}", filename);
                return;
            }

            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
            scanner.useDelimiter("\\A");
            String script = scanner.hasNext() ? scanner.next() : "";

            String[] statements = script.split(";");

            try (Connection conn = dbProvider.getConnection();
                Statement stmt = conn.createStatement()) {
                for (String sql : statements) {
                    if (!sql.trim().isBlank()) {
                        stmt.execute(sql);
                    }
                }

                log.debug("Initialize schema successfully: {}", filename);
            }
        } catch (Exception e) {
            throw new CoreException("Error occurred while initializing schema.", e);
        }
    }
}
