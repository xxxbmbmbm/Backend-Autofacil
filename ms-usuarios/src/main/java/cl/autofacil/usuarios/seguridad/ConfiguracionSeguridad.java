package cl.autofacil.usuarios.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity // habilita @PreAuthorize en los controladores
public class ConfiguracionSeguridad {

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http, FiltroJwt filtroJwt) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // API sin cookies: el token viaja en el encabezado Authorization
                .cors(cors -> cors.configurationSource(configuracionCors()))
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // "/error" debe ser público: ahí Spring reenvía los 403; si no, el cliente recibiría 401
                        .requestMatchers("/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/registro", "/api/v1/auth/login",
                                "/api/v1/auth/renovar", "/api/v1/auth/cerrar-sesion").permitAll()
                        .requestMatchers("/api/v1/usuarios/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(errores -> errores.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder cifradorClaves() {
        return new BCryptPasswordEncoder();
    }

    /** Igual que la guía del curso: cualquier origen, sin credenciales (el token va en un encabezado, no en cookies). */
    @Bean
    public CorsConfigurationSource configuracionCors() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(List.of("*"));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        configuracion.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", configuracion);
        return fuente;
    }
}
