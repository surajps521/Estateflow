package com.realestate.crm;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
class JwtTokenProvider {
  private final SecretKey key; private final long expiration;
  JwtTokenProvider(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.expiration-ms}") long expiration){
    this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expiration=expiration;
  }
  String generate(User user){Date now=new Date();return Jwts.builder().subject(user.getId().toString()).claim("email",user.getEmail()).claim("role",user.getRole().name()).issuedAt(now).expiration(new Date(now.getTime()+expiration)).signWith(key).compact();}
  Claims parse(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();}
}
@Component
class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository users; CustomUserDetailsService(UserRepository users){this.users=users;}
  public UserDetails loadUserByUsername(String email){User u=users.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("User not found"));
    if(!u.isActive())throw new UsernameNotFoundException("User is inactive");
    return org.springframework.security.core.userdetails.User.withUsername(u.getEmail()).password(u.getPasswordHash()).authorities("ROLE_"+u.getRole().name()).build();}
}
@Component
class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtTokenProvider tokens; private final UserRepository users;
  JwtAuthenticationFilter(JwtTokenProvider tokens,UserRepository users){this.tokens=tokens;this.users=users;}
  protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
    String h=req.getHeader("Authorization");
    if(h!=null&&h.startsWith("Bearer ")&&org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()==null){
      try {var claims=tokens.parse(h.substring(7));String email=claims.get("email",String.class);User u=users.findByEmail(email).orElseThrow();
        var auth=new UsernamePasswordAuthenticationToken(u.getEmail(),null,java.util.List.of(new SimpleGrantedAuthority("ROLE_"+u.getRole().name())));
        req.setAttribute("currentUserId",u.getId());org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
      } catch(Exception ignored){org.springframework.security.core.context.SecurityContextHolder.clearContext();}
    } chain.doFilter(req,res);
  }
}
@Configuration
class SecurityConfig {
  @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
  @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration c)throws Exception{return c.getAuthenticationManager();}
  @Bean SecurityFilterChain filterChain(HttpSecurity http,JwtAuthenticationFilter jwt)throws Exception{
    return http.csrf(c->c.disable()).cors(Customizer.withDefaults()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(a->a.requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
        .requestMatchers("/api/v1/auth/login","/api/v1/auth/register/customer","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
        .requestMatchers(HttpMethod.POST,"/api/v1/auth/register").hasRole("ADMIN")
        .requestMatchers(HttpMethod.POST,"/api/v1/properties").hasRole("ADMIN")
        .requestMatchers(HttpMethod.GET,"/api/v1/leads/my","/api/v1/site-visits/my","/api/v1/deals/my","/api/v1/recommendations/my").hasRole("CUSTOMER")
        .requestMatchers("/api/v1/agents/**","/api/v1/agent-requests/**","/api/v1/notifications/**").authenticated()
        .requestMatchers("/api/v1/leads/**","/api/v1/site-visits/**","/api/v1/deals/**","/api/v1/dashboard/**").hasAnyRole("ADMIN","AGENT")
        .anyRequest().authenticated())
      .addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();
  }
  @Bean CorsConfigurationSource corsConfigurationSource(){
    CorsConfiguration c=new CorsConfiguration();
    c.setAllowedOriginPatterns(java.util.List.of("http://localhost:*","http://127.0.0.1:*"));
    c.setAllowedMethods(java.util.List.of("GET","POST","PATCH","PUT","DELETE","OPTIONS","HEAD"));
    c.setAllowedHeaders(java.util.List.of("Authorization","Content-Type","Accept","Origin","X-Requested-With"));
    c.setExposedHeaders(java.util.List.of("Authorization"));
    c.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;
  }
}
