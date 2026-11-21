package com.yanki.auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.yanki.auth.domain.exception.BusinessException;
import com.yanki.auth.domain.model.Credential;
import com.yanki.auth.domain.model.LoginCommand;
import com.yanki.auth.domain.model.RefreshCommand;
import com.yanki.auth.domain.model.TokenClaims;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.test.StepVerifier;

/**
 * Pruebas unitarias del servicio de autenticacion.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "Secret-123";
    private static final String PASSWORD_HASH = "$2a$10$hashedPassword";
    private static final String CUSTOMER_ID = "CUSTOMER-1";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final long EXPIRATION_SECONDS = 3600L;

    @Mock
    private CredentialService credentialService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldIssueTokensWhenCredentialsAreValid() {
        Credential credential = credential();
        when(credentialService.findByUsername(USERNAME)).thenReturn(Maybe.just(credential));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(credentialService.recordLogin(credential)).thenReturn(Completable.complete());
        when(jwtService.generateAccessToken(credential)).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(credential)).thenReturn(REFRESH_TOKEN);
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(EXPIRATION_SECONDS);

        StepVerifier.create(authService.login(new LoginCommand(USERNAME, PASSWORD)).toFlowable())
                .assertNext(tokens -> {
                    assertThat(tokens.accessToken()).isEqualTo(ACCESS_TOKEN);
                    assertThat(tokens.refreshToken()).isEqualTo(REFRESH_TOKEN);
                    assertThat(tokens.tokenType()).isEqualTo("Bearer");
                    assertThat(tokens.expiresIn()).isEqualTo(EXPIRATION_SECONDS);
                    assertThat(tokens.username()).isEqualTo(USERNAME);
                    assertThat(tokens.roles()).containsExactly("ROLE_CUSTOMER");
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectLoginWhenPasswordIsWrong() {
        Credential credential = credential();
        when(credentialService.findByUsername(USERNAME)).thenReturn(Maybe.just(credential));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(false);

        StepVerifier.create(authService.login(new LoginCommand(USERNAME, PASSWORD)).toFlowable())
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) error).getCode()).isEqualTo("INVALID_CREDENTIALS");
                })
                .verify();
    }

    @Test
    void shouldRejectLoginWhenUserDoesNotExist() {
        when(credentialService.findByUsername(USERNAME)).thenReturn(Maybe.empty());

        StepVerifier.create(authService.login(new LoginCommand(USERNAME, PASSWORD)).toFlowable())
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) error).getCode()).isEqualTo("INVALID_CREDENTIALS");
                })
                .verify();
    }

    @Test
    void shouldRejectLoginWhenCredentialIsDisabled() {
        Credential credential = credential();
        credential.setEnabled(false);
        when(credentialService.findByUsername(USERNAME)).thenReturn(Maybe.just(credential));

        StepVerifier.create(authService.login(new LoginCommand(USERNAME, PASSWORD)).toFlowable())
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) error).getCode()).isEqualTo("INVALID_CREDENTIALS");
                })
                .verify();
    }

    @Test
    void shouldIssueNewAccessTokenWhenRefreshTokenIsValid() {
        Credential credential = credential();
        TokenClaims claims = new TokenClaims(USERNAME, CUSTOMER_ID, List.of("ROLE_CUSTOMER"), JwtService.TYPE_REFRESH,
                Instant.now(), Instant.now().plusSeconds(86400));
        when(jwtService.parseToken(REFRESH_TOKEN, JwtService.TYPE_REFRESH)).thenReturn(claims);
        when(credentialService.findByUsername(USERNAME)).thenReturn(Maybe.just(credential));
        when(credentialService.recordLogin(credential)).thenReturn(Completable.complete());
        when(jwtService.generateAccessToken(credential)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(credential)).thenReturn("new-refresh-token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(EXPIRATION_SECONDS);

        StepVerifier.create(authService.refresh(new RefreshCommand(REFRESH_TOKEN)).toFlowable())
                .assertNext(tokens -> {
                    assertThat(tokens.accessToken()).isEqualTo("new-access-token");
                    assertThat(tokens.refreshToken()).isEqualTo("new-refresh-token");
                    assertThat(tokens.username()).isEqualTo(USERNAME);
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectRefreshWhenTokenIsInvalid() {
        when(jwtService.parseToken(REFRESH_TOKEN, JwtService.TYPE_REFRESH))
                .thenThrow(new BusinessException("INVALID_TOKEN", "El token es invalido"));

        StepVerifier.create(authService.refresh(new RefreshCommand(REFRESH_TOKEN)).toFlowable())
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) error).getCode()).isEqualTo("INVALID_TOKEN");
                })
                .verify();
    }

    @Test
    void shouldReturnClaimsWhenAccessTokenIsValid() {
        TokenClaims claims = new TokenClaims(USERNAME, CUSTOMER_ID, List.of("ROLE_CUSTOMER"), JwtService.TYPE_ACCESS,
                Instant.now(), Instant.now().plusSeconds(3600));
        when(jwtService.parseToken(ACCESS_TOKEN, JwtService.TYPE_ACCESS)).thenReturn(claims);

        StepVerifier.create(authService.validate(ACCESS_TOKEN).toFlowable())
                .assertNext(result -> {
                    assertThat(result.valid()).isTrue();
                    assertThat(result.username()).isEqualTo(USERNAME);
                    assertThat(result.customerId()).isEqualTo(CUSTOMER_ID);
                    assertThat(result.roles()).containsExactly("ROLE_CUSTOMER");
                })
                .verifyComplete();
    }

    private Credential credential() {
        return Credential.builder()
                .id("CREDENTIAL-1")
                .username(USERNAME)
                .passwordHash(PASSWORD_HASH)
                .customerId(CUSTOMER_ID)
                .email("john.doe@yanki.pe")
                .roles(List.of("ROLE_CUSTOMER"))
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
