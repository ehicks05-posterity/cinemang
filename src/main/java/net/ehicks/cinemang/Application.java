package net.ehicks.cinemang;

import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application
{
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private DatabasePopulator databasePopulator;
    private GenreLoader genreLoader;
    private LanguageLoader languageLoader;

    @Autowired
    public Application(DatabasePopulator databasePopulator, GenreLoader genreLoader, LanguageLoader languageLoader)
    {
        this.databasePopulator = databasePopulator;
        this.genreLoader = genreLoader;
        this.languageLoader = languageLoader;
    }

    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx)
    {
        return args -> {
            SystemInfo.setLoadDbToRam(true);

            try
            {
                databasePopulator.populateDatabase();
                genreLoader.getUniqueGenres();
                languageLoader.getUniqueLanguages();
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        };
    }

    @Bean
    public TomcatServletWebServerFactory tomcatFactory()
    {
        return new TomcatServletWebServerFactory()
        {
            @Override
            protected void postProcessContext(Context context)
            {
                ((StandardJarScanner) context.getJarScanner()).setScanManifest(false);
            }
        };
    }
}