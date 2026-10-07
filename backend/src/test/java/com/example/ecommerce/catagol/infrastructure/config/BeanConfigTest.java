package com.example.ecommerce.catagol.infrastructure.config;

import com.example.ecommerce.catagol.application.model.AuthenticatedUser;
import com.example.ecommerce.catagol.application.model.Login;
import com.example.ecommerce.catagol.application.model.Product;
import com.example.ecommerce.catagol.application.model.Rating;
import com.example.ecommerce.catagol.application.port.in.LoginCommand;
import com.example.ecommerce.catagol.application.port.in.ProductNoteCreateCommand;
import com.example.ecommerce.catagol.application.port.out.*;
import com.example.ecommerce.catagol.application.service.AuthenticationService;
import com.example.ecommerce.catagol.application.service.AuditLogService;
import com.example.ecommerce.catagol.application.service.ProductNoteService;
import com.example.ecommerce.catagol.application.service.ProductService;
import com.example.ecommerce.catagol.domain.model.AuditLog;
import com.example.ecommerce.catagol.domain.model.ProductNote;
import com.example.ecommerce.catagol.domain.model.User;
import com.example.ecommerce.catagol.domain.exception.EmptyProductAPIException;
import com.example.ecommerce.catagol.domain.exception.ProductNoteDuplicateException;
import com.example.ecommerce.catagol.infrastructure.adapter.out.security.CustomUserDetails;
import com.example.ecommerce.catagol.infrastructure.adapter.out.security.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeanConfigTest {

  @Mock
  private UserRepositoryPort userRepositoryPort;

  @Mock
  private ExternalCatalogPort externalCatalogPort;

  @Mock
  private ProductNoteRepositoryPort productNoteRepositoryPort;

  @Mock
  private AuditLogRepositoryPort auditLogRepositoryPort;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private TokenProviderPort tokenProvider;

  private BeanConfig beanConfig;

  @BeforeEach
  void setUp() {
    beanConfig = new BeanConfig();
  }

  @Test
  void createsUserDetailsServiceConnectedToUserRepository() {
    var user = User.builder()
      .id(7L)
      .username("jane.doe")
      .password("encoded-password")
      .roles(List.of())
      .build();
    when(userRepositoryPort.findByUsername("jane.doe")).thenReturn(Optional.of(user));

    var userDetailsService = beanConfig.customUserDetailsService(userRepositoryPort);
    var userDetails = userDetailsService.loadUserByUsername("jane.doe");

    assertInstanceOf(CustomUserDetailsService.class, userDetailsService);
    assertEquals("jane.doe", userDetails.getUsername());
    assertEquals("encoded-password", userDetails.getPassword());
    verify(userRepositoryPort).findByUsername("jane.doe");
  }

  @Test
  void userDetailsServiceReportsWhenUserIsNotFound() {
    when(userRepositoryPort.findByUsername("unknown")).thenReturn(Optional.empty());
    var userDetailsService = beanConfig.customUserDetailsService(userRepositoryPort);

    assertThrows(UsernameNotFoundException.class,
      () -> userDetailsService.loadUserByUsername("unknown")
    );
    verify(userRepositoryPort).findByUsername("unknown");
  }

  @Test
  void createsProductUseCaseConnectedToCatalogNotesAndAuditRepositories() {
    var product = new Product(
      1L,
      "Chair",
      new BigDecimal("49.99"),
      "Comfortable chair",
      "Furniture",
      new Rating(4.5, 12)
    );
    when(externalCatalogPort.fetchAllProducts()).thenReturn(List.of(product));
    when(productNoteRepositoryPort.findAll()).thenReturn(List.of());

    var productUseCase = beanConfig.productUseCase(
      externalCatalogPort,
      productNoteRepositoryPort
    );
    var products = productUseCase.getEnrichedCatalog();

    assertInstanceOf(ProductService.class, productUseCase);
    assertEquals(1, products.size());
    assertEquals(1L, products.get(0).id());
    assertEquals("Chair", products.get(0).title());
    assertEquals("", products.get(0).note());
    verify(externalCatalogPort).fetchAllProducts();
    verify(productNoteRepositoryPort).findAll();
  }

  @Test
  void productUseCasePropagatesEmptyCatalogFailure() {
    when(externalCatalogPort.fetchAllProducts()).thenReturn(List.of());
    var productUseCase = beanConfig.productUseCase(
      externalCatalogPort,
      productNoteRepositoryPort
    );

    assertThrows(EmptyProductAPIException.class, productUseCase::getEnrichedCatalog);

    verify(externalCatalogPort).fetchAllProducts();
    verify(productNoteRepositoryPort, never()).findAll();
    verify(auditLogRepositoryPort, never()).save(any());
  }

  @Test
  void createsAuthenticationServiceConnectedToAuthenticationAndTokenProviders() {
    var loginCommand = new LoginCommand("jane.doe", "correct-password");
    var jwtConfig = new SecurityJwtConfig();
    jwtConfig.setExpiration(900L);
    var customUserDetails = new CustomUserDetails(loginCommand.username(), loginCommand.password(),
      List.of(new SimpleGrantedAuthority("ROLE_USER")), "Jane", "Doe");

    when(authenticationManager.authenticate(any()))
      .thenReturn(UsernamePasswordAuthenticationToken.authenticated(
        customUserDetails,
        null,
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
      ));
    when(tokenProvider.generateToken(any(AuthenticatedUser.class))).thenReturn("signed-token");

    AuthenticationService authenticationService = beanConfig.authenticationService(
      authenticationManager,
      tokenProvider,
      jwtConfig
    );

    Login response = authenticationService.authenticate(loginCommand);

    assertInstanceOf(AuthenticationService.class, authenticationService);
    assertEquals("signed-token", response.accessToken());
    assertEquals("Bearer", response.tokenType());
    assertEquals(900L, response.expiresIn());
    var userCaptor = ArgumentCaptor.forClass(AuthenticatedUser.class);
    verify(tokenProvider).generateToken(userCaptor.capture());
    assertEquals("jane.doe", userCaptor.getValue().userId());
    assertEquals("jane.doe", userCaptor.getValue().username());
    assertEquals(Set.of("ROLE_USER"), userCaptor.getValue().roles());
  }

  @Test
  void authenticationServicePropagatesInvalidCredentialFailures() {
    var authenticationException = new BadCredentialsException("Invalid credentials");
    when(authenticationManager.authenticate(any())).thenThrow(authenticationException);
    var authenticationService = beanConfig.authenticationService(
      authenticationManager,
      tokenProvider,
      new SecurityJwtConfig()
    );

    var loginCommand = new LoginCommand("jane.doe", "wrong-password");
    var exception = assertThrows(BadCredentialsException.class,
      () -> authenticationService.authenticate(loginCommand));

    assertSame(authenticationException, exception);
    verify(tokenProvider, never()).generateToken(any(AuthenticatedUser.class));
  }

  @Test
  void createsProductNoteServiceConnectedToProductNoteRepository() {
    var productNoteCreateCommand = new ProductNoteCreateCommand(42L, "Durable material",
      "John Dave");
    when(productNoteRepositoryPort.save(any(ProductNote.class)))
      .thenAnswer(invocation -> {
        ProductNote productNote = invocation.getArgument(0);
        productNote.setNoteId(7L);
        return productNote;
      });

    ProductNoteService productNoteService = beanConfig.productNoteService(productNoteRepositoryPort);
    ProductNote savedProductNote = productNoteService.saveProductNote(productNoteCreateCommand);

    var productNote = ProductNote.builder()
      .createdBy("John Dave")
      .note("Durable material")
      .extProdId(42L)
      .noteId(7L)
      .build();
    assertInstanceOf(ProductNoteService.class, productNoteService);
    assertEquals(productNote, savedProductNote);
    verify(productNoteRepositoryPort).save(any(ProductNote.class));
  }

  @Test
  void productNoteServicePropagatesDuplicateNoteFailures() {
    var productNoteCreateCommand = new ProductNoteCreateCommand(42L, "Durable material",
      "John Dave");
    var duplicateException = new ProductNoteDuplicateException("The Product Note is duplicate");
    when(productNoteRepositoryPort.save(any(ProductNote.class))).thenThrow(duplicateException);
    ProductNoteService productNoteService = beanConfig.productNoteService(productNoteRepositoryPort);

    var exception = assertThrows(ProductNoteDuplicateException.class,
      () -> productNoteService.saveProductNote(productNoteCreateCommand)
    );

    assertSame(duplicateException, exception);
    verify(productNoteRepositoryPort).save(any(ProductNote.class));
  }

  @Test
  void createsAuditLogServiceConnectedToAuditLogRepository() {
    var auditLog = AuditLog.builder()
      .auditLogId(11L)
      .operation("GET PRODUCTS API")
      .status("SUCCESS")
      .durationMs(42L)
      .registerDate(LocalDateTime.of(2026, 9, 29, 22, 0))
      .createdBy("API")
      .build();
    when(auditLogRepositoryPort.findAll()).thenReturn(List.of(auditLog));

    AuditLogService auditLogService = beanConfig.auditLogService(auditLogRepositoryPort);

    List<AuditLog> responses = auditLogService.findAll();

    assertInstanceOf(AuditLogService.class, auditLogService);
    assertEquals(

      List.of(new AuditLog(
      11L,
      "GET PRODUCTS API",
      "SUCCESS",
      42L,
      LocalDateTime.of(2026, 9, 29, 22, 0),
      "API",
      null
    )), responses);
    verify(auditLogRepositoryPort).findAll();
  }


}
