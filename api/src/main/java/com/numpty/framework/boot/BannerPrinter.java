package com.numpty.framework.boot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BannerPrinter {
    private static final Logger log = LoggerFactory.getLogger(BannerPrinter.class);

    public void printBanner() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("banner.logo")) {
            if (inputStream == null) {
                System.out.println("SprinT Engine Starting...");
                System.out.println("                   v1.0.0");
                return;
            }

            String banner = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("${B}", "\u001B[1;34m")
                    .replace("${R}", "\u001B[0m");

            System.out.println(banner);
        } catch (IOException e) {
            log.warn("Failed to load banner: {}", e.getMessage());
        }
    }
}
