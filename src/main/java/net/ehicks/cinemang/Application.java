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
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@SpringBootApplication
@EnableScheduling
public class Application
{
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private Seeder seeder;

    @Autowired
    public Application(Seeder seeder)
    {
        this.seeder = seeder;
    }

    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx)
    {
        return args -> {
            try
            {
                seeder.runTask();
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

    @Configuration
    public static class SecurityPermitAllConfig extends WebSecurityConfigurerAdapter
    {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().anyRequest().permitAll()
                    .and().csrf().disable();
        }
    }
}