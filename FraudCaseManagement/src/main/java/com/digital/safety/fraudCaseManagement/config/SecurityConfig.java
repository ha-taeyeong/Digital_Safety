package com.digital.safety.fraudCaseManagement.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                // 0. HTML 렌더링을 위한 내부 이동 허용
                .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()

                // 1. 정적 리소스(CSS, JS, 이미지 등) 허용
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico", "/error").permitAll()

                // [중요 변경] 2. 게시글 관리(등록/수정/삭제) -> 누구나 접근 허용 (비밀번호로 검증함)
                // 화면 URL (/cases/new, /cases/save 등)
                .requestMatchers("/cases/**").permitAll()
                // API URL (/api/cases 등)
                .requestMatchers("/api/cases/**").permitAll()

                // 3. 기타 공개 페이지 (메인, 게임, 카테고리별 목록)
                .requestMatchers("/", "/login", "/game", "/api/game/**").permitAll()
                .requestMatchers("/gov/**", "/tele/**", "/finance/**").permitAll()

                // 4. 그 외 요청은 안전을 위해 인증 요구 (관리자 전용 API 등)
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/login")          // 커스텀 로그인 페이지
                .defaultSuccessUrl("/", true) // 로그인 성공 시 메인으로
                .permitAll()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    // 인증된 사용자의 정보(authentication.getName() 등)를 통해 관리자인지 확인
                    if ("admin".equals(authentication.getName())) {
                        request.getSession().setAttribute("isAdmin", true);
                    } else {
                        request.getSession().setAttribute("isAdmin", false);
                    }
                    response.sendRedirect("/"); // 로그인 성공 후 리디렉션
                })
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutSuccessUrl("/")        // 로그아웃 시 메인으로
                .permitAll()
            )
            // [필수] POST 요청(등록/수정/삭제)을 위해 CSRF 비활성화
            .csrf((csrf) -> csrf.disable());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // 관리자 계정 (혹시 관리자 로그인이 필요할 때 사용)
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("1234")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}