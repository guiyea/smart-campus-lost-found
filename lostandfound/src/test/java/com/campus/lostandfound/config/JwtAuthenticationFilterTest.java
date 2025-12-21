package com.campus.lostandfound.config;

import com.campus.lostandfound.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * JWT认证过滤器测试类
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_WithValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        Long userId = 1L;
        Integer role = 0;

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtUtil.getRoleFromToken(token)).thenReturn(role);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication, "Authentication should be set in SecurityContext");
        assertEquals(userId, authentication.getPrincipal(), "Principal should be userId");
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")), "Should have USER role");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WithAdminRole_ShouldSetAdminAuthority() throws ServletException, IOException {
        // Given
        String token = "admin.jwt.token";
        Long userId = 2L;
        Integer role = 1;

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtUtil.getRoleFromToken(token)).thenReturn(role);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication, "Authentication should be set in SecurityContext");
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")), "Should have ADMIN role");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        String token = "invalid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.validateToken(token)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set for invalid token");

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).getUserIdFromToken(anyString());
    }

    @Test
    void testDoFilterInternal_WithNoAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given - no Authorization header

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set when no Authorization header");

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void testDoFilterInternal_WithMalformedAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "InvalidFormat token");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set for malformed header");

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void testDoFilterInternal_WithEmptyBearerToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer ");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set for empty token");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WithTokenParsingException_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Given
        String token = "malformed.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.validateToken(token)).thenThrow(new RuntimeException("Token parsing failed"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Authentication should not be set when token parsing fails");

        verify(filterChain).doFilter(request, response);
    }
}
