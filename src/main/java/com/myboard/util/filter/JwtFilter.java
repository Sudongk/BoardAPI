package com.myboard.util.filter;

import com.myboard.jwt.JwtTokenManager;
import com.myboard.util.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final JwtTokenManager jwtTokenManager;

    public JwtFilter(JwtProvider jwtProvider,
                     @Lazy UserDetailsService userDetailsService,
                     JwtTokenManager jwtTokenManager) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
        this.jwtTokenManager = jwtTokenManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = jwtProvider.getJwtFromRequest(request);
        String username = jwtProvider.extractUsername(token);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtProvider.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return StringUtils.startsWithAny(request.getRequestURI(), "/api/v1/user/create", "/api/v1/user/login");
    }

//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        String jwtToken = getJwtToken(request);
//
//        log.info("jwtFilter doFilterInternal : {}", jwtToken);
//
//        Map<String, Object> claims = jwtProvider.parseClaims(jwtToken);
//
//        SecurityContextHolder.getContext().setAuthentication(createAuthentication(claims));
//        filterChain.doFilter(request, response);
//    }
//
//    private Authentication createAuthentication(Map<String, Object> claims) {
//        List<SimpleGrantedAuthority> roles = Arrays.stream(claims.get("roles").toString().split(","))
//                .map(SimpleGrantedAuthority::new)
//                .collect(Collectors.toList());
//
//        return new UsernamePasswordAuthenticationToken(claims.get(Claims.SUBJECT), null, roles);
//    }
}
