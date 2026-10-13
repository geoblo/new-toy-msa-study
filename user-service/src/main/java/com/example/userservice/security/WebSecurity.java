package com.example.userservice.security;

import com.example.userservice.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurity {
    private UserService userService;
    private Environment env;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public static final String ALLOWED_IP_ADDRESS = "127.0.0.1";
    public static final String SUBNET = "/32";
    public static final IpAddressMatcher ALLOWED_IP_ADDRESS_MATCHER = new IpAddressMatcher(ALLOWED_IP_ADDRESS + SUBNET);

    public WebSecurity(Environment env, UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.env = env;
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);

        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        http.csrf( (csrf) -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/h2-console/**").permitAll()  // 특정 경로 허용
                    .requestMatchers("/actuator/**").permitAll()  // 특정 경로 허용
                    .requestMatchers("/health-check/**").permitAll()  // 특정 경로 허용
//                    .requestMatchers(HttpMethod.POST, "/users").permitAll() // 회원가입 - 인증 불필요
                    .requestMatchers("/**").access( // **(모든 경로)에 대해 지정한 IP에서 온 요청만 허용하고 다른 IP에서 오면 403으로 거절
                            new WebExpressionAuthorizationManager( // 보통 단일 진입점인 API Gateway의 IP 주소만 지정
                                    "hasIpAddress('127.0.0.1') or hasIpAddress('::1') or " +
                                    "hasIpAddress('172.20.0.1')")) // host pc ip address (해당하는 IP 주소만 접근을 허용)
                                    // 터미널 실행 ->
                                    // Windows: ipconfig -> 무선 LAN 어댑터 Wi-Fi 또는 이더넷 어댑터 vEthernet 주소 복사해오기
                                    // macOS: ifconfig -> en0의 inet 주소 복사해오기
                    .anyRequest().authenticated()              // 그 외는 인증 필요
                    // (참고) 앞의 /**가 모든 요청을 잡기 때문에 실행될 일이 없음
                )
            .authenticationManager(authenticationManager)
            .addFilter(getAuthenticationFilter(authenticationManager))
            .httpBasic(Customizer.withDefaults())  // ← Basic 인증 추가
            .headers((headers) -> headers
                .frameOptions((frameOptions) -> frameOptions.sameOrigin()));

        return http.build();
    }

    private AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager, userService, env);
        authenticationFilter.setAuthenticationManager(authenticationManager);

        return authenticationFilter;
    }
}
