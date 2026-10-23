package com.tfmsp.backend.config;

import java.io.IOException;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.config.Customizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tfmsp.backend.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource(
	        @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}") String origins) {
	    CorsConfiguration cfg = new CorsConfiguration();
	    cfg.setAllowedOrigins(Arrays.asList(origins.split(",")));
	    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	    cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
	    cfg.setExposedHeaders(List.of("X-Total-Count"));
	    cfg.setMaxAge(3600L);
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", cfg);
	    return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable())
		
		.cors(Customizer.withDefaults())
		
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth
					    .requestMatchers("/api/auth/change-password").authenticated()
					    .requestMatchers(
					        "/api/auth/**",
					        "/api/tenants/register",
					        "/actuator/health",
					        "/error"
					    ).permitAll()
					    .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
					    .requestMatchers("/api/host/**").hasAnyRole("HOST", "SUPER_ADMIN")
					    .anyRequest().authenticated()
					)

				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(
								(req, res, e) -> writeError(res, 401, "UNAUTHORIZED", "Authentication required"))
						.accessDeniedHandler((req, res, e) -> writeError(res, 403, "FORBIDDEN", "Access denied")))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	private void writeError(HttpServletResponse res, int status, String code, String message) throws IOException {
		res.setStatus(status);
		res.setContentType("application/json");
		res.getWriter().write("{\"status\":" + status + ",\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
	}
}