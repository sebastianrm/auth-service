
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AuthServiceTest {

    @Mock
    private ExternalSystemClient externalSystemClient;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHappyPathLoginSuccess() {
        // Preparar mock para sistema externo
        when(externalSystemClient.validateToken(anyString())).thenReturn(true);

        // Llamada al método a probar
        boolean result = authService.login("user", "password");

        // Validar resultado exitoso
        assertTrue(result, "Login debería ser exitoso con credenciales válidas");
    }

    @Test
    public void testLoginFailureInvalidCredentials() {
        when(externalSystemClient.validateToken(anyString())).thenReturn(false);

        boolean result = authService.login("user", "wrongPassword");

        assertFalse(result, "Login debería fallar con credenciales inválidas");
    }

    @Test
    public void testLoginExternalServiceError() {
        when(externalSystemClient.validateToken(anyString())).thenThrow(new RuntimeException("External system down"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login("user", "password");
        });

        assertEquals("External system down", exception.getMessage());
    }

}
