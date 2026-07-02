package cl.duoc.itinerary.config;

import cl.duoc.itinerary.client.AuthClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthClient authClient;

    public SecurityConfig(AuthClient authClient) {
        this.authClient = authClient;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publicos (Swagger)
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html",
                                "/webjars/**"
                        ).permitAll()
                        // Todo lo demas requiere token valido
                        .anyRequest().authenticated()
                )
                // Filtro de validacion JWT antes del filtro de autenticacion por usuario/contrasena
                .addFilterBefore(new TokenValidationFilter(authClient),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
