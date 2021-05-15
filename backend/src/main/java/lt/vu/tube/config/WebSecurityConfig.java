package lt.vu.tube.config;

import lombok.AllArgsConstructor;
import lt.vu.tube.jwt.JwtConfig;
import lt.vu.tube.jwt.JwtTokenVerifier;
import lt.vu.tube.jwt.JwtUsernameAndPasswordAuthenticationFilter;
import lt.vu.tube.services.AppUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.crypto.SecretKey;

import static org.springframework.security.config.Customizer.withDefaults;
import static lt.vu.tube.enums.AppUserRole.*;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
@EnableWebMvc
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final AppUserService appUserService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SecretKey secretKey;
    private final JwtConfig jwtConfig;

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .cors(withDefaults())
//                .csrf().disable()
//                .authorizeRequests()
//                .antMatchers("/api/v*/registration/**").permitAll()
//                .antMatchers("/", "index", "/css/*", "/js/*").permitAll()
//                .antMatchers("/**").permitAll()
//                .anyRequest()
//                .authenticated().and()
//                .formLogin().defaultSuccessUrl("/videos")
//                .and()
//                .logout()
//                .logoutUrl("/logout")
//                .clearAuthentication(true)
//                .invalidateHttpSession(true)
//                .logoutSuccessUrl("/login");
//    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(new JwtUsernameAndPasswordAuthenticationFilter(authenticationManager(), jwtConfig, secretKey))
                .addFilterAfter(new JwtTokenVerifier(secretKey, jwtConfig),JwtUsernameAndPasswordAuthenticationFilter.class)
                .authorizeRequests()
                .anyRequest()
                .authenticated();
    }

    //HttpSecurity ignoravo mano maldas praleis šitą endpoint
    //WebSecurity ima priority tik galima problema kad šito request neauthentifikuoja
    //Net ir prisijungus tai loguose visada bus anonymous
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/video/share/get/*", "/api/v*/registration/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        provider.setUserDetailsService(appUserService);
        return provider;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }
}
