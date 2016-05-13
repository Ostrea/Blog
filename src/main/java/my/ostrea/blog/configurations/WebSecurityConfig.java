package my.ostrea.blog.configurations;

import my.ostrea.blog.models.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/").permitAll()
            .antMatchers("/administrator_control_panel").hasRole("ADMIN")
            .antMatchers("/css/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .formLogin()
            .permitAll()
            .and()
            .logout().permitAll().logoutSuccessUrl("/")
            .and()
            .csrf();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username).map(user -> {

            // Need to use StringUtils from Spring cause AbstractCollection::toString returns
            // string with brackets and authorities couldn't be created from such representation
            String commaDelimitedRoles =
                    StringUtils.collectionToCommaDelimitedString(user.getUserRoles());
            List<GrantedAuthority> grantedAuthorities =
                    AuthorityUtils.commaSeparatedStringToAuthorityList(commaDelimitedRoles);
            return new User(user.getUsername(), user.getPassword(),
                    user.isEnabled(), true, true, true, grantedAuthorities);
        }).orElseThrow(() -> new UsernameNotFoundException("User " + username + " was not found in the database"));
    }

    @Configuration
    protected static class ThymeleafSecurityDialectConfiguration {
        @Bean
        public SpringSecurityDialect securityDialect() {
            return new SpringSecurityDialect();
        }

    }
}