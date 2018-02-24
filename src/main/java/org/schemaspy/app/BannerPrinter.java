package org.schemaspy.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class BannerPrinter {

    static void printBanner() {
        String version = ApplicationContextInfo.getVersion();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(SchemaSpy.class.getResourceAsStream("/banner.txt"), StandardCharsets.UTF_8))) {
            String line;
            while((line = bufferedReader.readLine()) != null) {
                line = line.replace("${application.version}", version);
                System.out.println(line);
            }
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
