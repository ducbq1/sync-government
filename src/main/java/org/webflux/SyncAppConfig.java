package org.webflux;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

@Log4j2
@Configuration
public class SyncAppConfig implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Value("${fundamental.app.path.database}")
    private String pathDatabase;

    public SyncAppConfig() {
        super();
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    @Bean
    public DataSource dataSource() {
        String databasePath = copyDatabaseToLocation();
        log.info("Database path: {}", databasePath);
        final DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.sqlite.JDBC");
        dataSourceBuilder.url("jdbc:sqlite:" + databasePath);
        return dataSourceBuilder.build();
    }

    private String copyDatabaseToLocation() {
        try {
            Resource resource = applicationContext.getResource("classpath:/data/chinook.sqlite");
            File tempFile = new File(Paths.get(System.getProperty("user.dir"), pathDatabase).toString());

            if (!tempFile.exists() && !tempFile.isDirectory()) {
                try (InputStream inputStream = resource.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
            }
            return tempFile.getAbsolutePath();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load SQLite database file", ex);
        }
    }

}