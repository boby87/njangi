package com.njangi.auth.service;

import com.njangi.auth.dto.*;
import com.njangi.auth.entity.StatutUtilisateur;
import com.njangi.auth.entity.Utilisateur;
import com.njangi.auth.event.UtilisateurEventPublisher;
import com.njangi.auth.exception.BusinessException;
import com.njangi.auth.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private OtpService otpService;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UtilisateurEventPublisher eventPublisher;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                utilisateurRepository,
                otpService,
                jwtService,
                passwordEncoder,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Inscrire un membre avec un numéro international (France / Diaspora)")
    void testInscrireMembreDiaspora() {
        InscriptionRequest request = new InscriptionRequest(
                "+33612345678",
                "diaspora@njangi.cm",
                "Secret123!",
                "Kamga",
                "Eric",
                null,
                "Paris",
                "France"
        );

        when(utilisateurRepository.existsByTelephone("+33612345678")).thenReturn(false);
        when(utilisateurRepository.existsByEmail("diaspora@njangi.cm")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("hashed_password");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtService.generateToken(any(Utilisateur.class))).thenReturn("fake.jwt.token");
        when(jwtService.getExpirationInSeconds()).thenReturn(86400L);

        LoginResponse response = authService.inscrire(request);

        assertNotNull(response);
        assertEquals("fake.jwt.token", response.accessToken());
        assertEquals("+33612345678", response.utilisateur().telephone());
        assertEquals("France", response.utilisateur().pays());
        verify(eventPublisher).publishUtilisateurInscrit(any(UUID.class), eq("+33612345678"), eq("Kamga Eric"));
    }

    @Test
    @DisplayName("Inscrire un membre avec un numéro local camerounais (préfixé automatiquement en +237)")
    void testInscrireMembreLocalCamerounais() {
        InscriptionRequest request = new InscriptionRequest(
                "699123456",
                "local@njangi.cm",
                "Secret123!",
                "Mbarga",
                "Paul",
                null,
                "Douala",
                "Cameroun"
        );

        when(utilisateurRepository.existsByTelephone("+237699123456")).thenReturn(false);
        when(utilisateurRepository.existsByEmail("local@njangi.cm")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("hashed_password");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtService.generateToken(any(Utilisateur.class))).thenReturn("fake.jwt.token");
        when(jwtService.getExpirationInSeconds()).thenReturn(86400L);

        LoginResponse response = authService.inscrire(request);

        assertNotNull(response);
        assertEquals("+237699123456", response.utilisateur().telephone());
    }

    @Test
    @DisplayName("Échec de connexion si mot de passe incorrect")
    void testConnexionMotDePasseIncorrect() {
        LoginRequest request = new LoginRequest("+237699123456", "WrongPassword");

        Utilisateur u = new Utilisateur();
        u.setId(UUID.randomUUID());
        u.setTelephone("+237699123456");
        u.setMotDePasseHash("hashed_correct_password");
        u.setStatut(StatutUtilisateur.ACTIF);

        when(utilisateurRepository.findByIdentifiant("+237699123456")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("WrongPassword", "hashed_correct_password")).thenReturn(false);

        assertThrows(BusinessException.class, () -> authService.connexion(request));
    }

    @Test
    @DisplayName("Connexion via Google avec création automatique (JIT provisioning)")
    void testConnexionGoogleJitProvisioning() {
        SocialLoginRequest request = new SocialLoginRequest(
                "GOOGLE",
                "mock_google_id_token",
                "google-sub-12345",
                "kamga.google@gmail.com",
                "Kamga",
                "Eric",
                "https://lh3.googleusercontent.com/photo.jpg"
        );

        when(utilisateurRepository.findByProviderAuthAndProviderId("GOOGLE", "google-sub-12345")).thenReturn(Optional.empty());
        when(utilisateurRepository.findByEmail("kamga.google@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("random_hashed_pass");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtService.generateToken(any(Utilisateur.class))).thenReturn("fake.jwt.google.token");
        when(jwtService.getExpirationInSeconds()).thenReturn(86400L);

        SocialLoginResponse response = authService.connexionSociale(request);

        assertNotNull(response);
        assertEquals("fake.jwt.google.token", response.accessToken());
        assertEquals("kamga.google@gmail.com", response.utilisateur().email());
        assertTrue(response.telephoneRequis(), "Le téléphone doit être requis pour un nouvel utilisateur Google");
        verify(eventPublisher).publishUtilisateurInscrit(any(UUID.class), eq("kamga.google@gmail.com"), eq("Kamga Eric"));
    }

    @Test
    @DisplayName("Connexion via Facebook avec compte déjà lié et numéro de téléphone présent")
    void testConnexionFacebookCompteExistant() {
        SocialLoginRequest request = new SocialLoginRequest(
                "FACEBOOK",
                "mock_fb_access_token",
                "fb-id-98765",
                "mbarga.fb@njangi.cm",
                "Mbarga",
                "Paul",
                null
        );

        Utilisateur u = new Utilisateur();
        u.setId(UUID.randomUUID());
        u.setProviderAuth("FACEBOOK");
        u.setProviderId("fb-id-98765");
        u.setEmail("mbarga.fb@njangi.cm");
        u.setTelephone("+237699123456");
        u.setStatut(StatutUtilisateur.ACTIF);

        when(utilisateurRepository.findByProviderAuthAndProviderId("FACEBOOK", "fb-id-98765")).thenReturn(Optional.of(u));
        when(jwtService.generateToken(u)).thenReturn("fake.jwt.fb.token");
        when(jwtService.getExpirationInSeconds()).thenReturn(86400L);

        SocialLoginResponse response = authService.connexionSociale(request);

        assertNotNull(response);
        assertEquals("fake.jwt.fb.token", response.accessToken());
        assertFalse(response.telephoneRequis(), "Le téléphone est déjà renseigné, telephoneRequis doit être false");
    }
}
