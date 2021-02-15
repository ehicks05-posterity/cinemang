package net.ehicks.cinemang.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class MultiSecurityConfig
{
    // @Value("${spring.security.user.name}")
    // String username;

    // @Value("${spring.security.user.password}")
    // String password;

    // @Bean
    // public UserDetailsService userDetailsService()
    // {
    //     // ensure the passwords are encoded properly
    //     User.UserBuilder users = User.withDefaultPasswordEncoder();
    //     InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
    //     manager.createUser(users.username(username).password(password).roles("USER","ADMIN").build());
    //     return manager;
    // }

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 9)
    public static class SecurityPermitAllConfig extends WebSecurityConfigurerAdapter
    {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().anyRequest().permitAll()
                    .and().csrf().disable();
        }
    }
}
