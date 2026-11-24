package com.njangi.identite.application.service;

import com.njangi.identite.application.dto.*;
import com.njangi.identite.domain.exception.BusinessException;
import com.njangi.identite.domain.model.StatutUtilisateur;
import com.njangi.identite.domain.model.Utilisateur;
import com.njangi.identite.domain.port.out.IdentiteEventPublisherPort;
import com.njangi.identite.domain.port.out.UtilisateurRepositoryPort;
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
    private UtilisateurRepositoryPort utilisateurRepository;
    @Mock
    private OtpService otpService;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private IdentiteEventPublisherPort eventPublisher;

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

        when(utilisateurRepository.existeParTelephone("+33612345678")).thenReturn(false);
        when(utilisateurRepository.existeParEmail("diaspora@njangi.cm")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("hashed_password");
        when(utilisateurRepository.sauvegarder(any(Utilisateur.class))).thenAnswer(invocation -> {
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

        when(utilisateurRepository.existeParTelephone("+237699123456")).thenReturn(false);
        when(utilisateurRepository.existeParEmail("local@njangi.cm")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("hashed_password");
        when(utilisateurRepository.sauvegarder(any(Utilisateur.class))).thenAnswer(invocation -> {
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
    @DisplayName("Échec d'inscription si le numéro de téléphone existe déjà")
    void testInscrireTelephoneExistant() {
        InscriptionRequest request = new InscriptionRequest(
                "+237699123456",
                "doublon@njangi.cm",
                "Secret123!",
                "Mbarga",
                "Paul",
                null,
                "Douala",
                "Cameroun"
        );

        when(utilisateurRepository.existeParTelephone("+237699123456")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.inscrire(request));
        verify(utilisateurRepository, never()).sauvegarder(any());
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

        when(utilisateurRepository.trouverParIdentifiant("+237699123456")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("WrongPassword", "hashed_correct_password")).thenReturn(false);

        assertThrows(BusinessException.class, () -> authService.connexion(request));
    }

    @Test
    @DisplayName("Échec de connexion si le compte est suspendu")
    void testConnexionCompteSuspendu() {
        LoginRequest request = new LoginRequest("+237699123456", "Secret123!");

        Utilisateur u = new Utilisateur();
        u.setId(UUID.randomUUID());
        u.setTelephone("+237699123456");
        u.setMotDePasseHash("hashed_password");
        u.setStatut(StatutUtilisateur.SUSPENDU);

        when(utilisateurRepository.trouverParIdentifiant("+237699123456")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("Secret123!", "hashed_password")).thenReturn(true);

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

        when(utilisateurRepository.trouverParProviderAuthEtProviderId("GOOGLE", "google-sub-12345")).thenReturn(Optional.empty());
        when(utilisateurRepository.trouverParEmail("kamga.google@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("random_hashed_pass");
        when(utilisateurRepository.sauvegarder(any(Utilisateur.class))).thenAnswer(invocation -> {
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
        verify(eventPublisher).publishUtilisateurConnecte(any(UUID.class), eq("GOOGLE:google-sub-12345"));
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

        when(utilisateurRepository.trouverParProviderAuthEtProviderId("FACEBOOK", "fb-id-98765")).thenReturn(Optional.of(u));
        when(jwtService.generateToken(u)).thenReturn("fake.jwt.fb.token");
        when(jwtService.getExpirationInSeconds()).thenReturn(86400L);

        SocialLoginResponse response = authService.connexionSociale(request);

        assertNotNull(response);
        assertEquals("fake.jwt.fb.token", response.accessToken());
        assertFalse(response.telephoneRequis(), "Le téléphone est déjà renseigné, telephoneRequis doit être false");
    }

    @Test
    @DisplayName("Liaison d'un numéro de téléphone avec code OTP valide")
    void testLierTelephoneAvecOtp() {
        UUID userId = UUID.randomUUID();
        Utilisateur u = new Utilisateur();
        u.setId(userId);
        u.setEmail("social@njangi.cm");
        u.setStatut(StatutUtilisateur.ACTIF);

        LierTelephoneRequest request = new LierTelephoneRequest("699123456", "123456");

        when(utilisateurRepository.trouverParId(userId)).thenReturn(Optional.of(u));
        when(utilisateurRepository.trouverParTelephone("+237699123456")).thenReturn(Optional.empty());
        when(otpService.verifierOtp("+237699123456", "123456")).thenReturn(true);
        when(utilisateurRepository.sauvegarder(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(Utilisateur.class))).thenReturn("linked.jwt.token");
        when(jwtService.getExpirationInSeconds()).thenReturn(86400L);

        LoginResponse response = authService.lierTelephone(userId, request);

        assertEquals("+237699123456", response.utilisateur().telephone());
        assertEquals("linked.jwt.token", response.accessToken());
    }
}
