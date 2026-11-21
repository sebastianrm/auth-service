# Community Labeling

Graphify is running in assistant/skill mode (no API key). You are the host
assistant (Claude Code / Codex / Gemini CLI). Read the community listing below
and write 2-5 word plain-language names for each.

## Language

Write every name in English (en). Do not switch languages.

## Communities

Community 0: JwtService, JwtService.java, .buildSigningKey(, .extractRoles(, .generateAccessToken(, .generateRefreshToken(, .getAccessTokenExpirationSeconds(, .getRefreshTokenExpirationSeconds(, .JwtService(, .parseToken(, .toInstant(
Community 1: JwtServiceTest, JwtServiceTest.java, .credential(, .setUp(, .shouldIncludeExpectedClaimsInGeneratedAccessToken(, .shouldIssueRefreshTokenWithRefreshType(, .shouldRejectAccessTokenWhenRefreshTokenIsExpected(, .shouldRejectExpiredToken(, .shouldRejectSecretShorterThanThirtyTwoBytes(, .shouldRejectTokenWithInvalidSignature(, .shouldSignTokensWithHs256Algorithm(
Community 2: AuthServiceTest, AuthServiceTest.java, .credential(, .shouldIssueNewAccessTokenWhenRefreshTokenIsValid(, .shouldIssueTokensWhenCredentialsAreValid(, .shouldRejectLoginWhenCredentialIsDisabled(, .shouldRejectLoginWhenPasswordIsWrong(, .shouldRejectLoginWhenUserDoesNotExist(, .shouldRejectRefreshWhenTokenIsInvalid(, .shouldReturnClaimsWhenAccessTokenIsValid(
Community 3: GlobalExceptionHandler, GlobalExceptionHandler.java, .buildResponse(, .handleBusinessException(, .handleDuplicateKeyException(, .handleInputException(, .handleUnexpectedException(, .handleValidationException(, .resolveStatus(
Community 4: ReactiveAdapter, ReactiveAdapter.java, .ReactiveAdapter(, .toCompletable(, .toFlowable(, .toMaybe(, .toSingle(
Community 5: ProcessedEventRepository, ProcessedEvent, ReactiveMongoRepository, ProcessedEventRepository.java, .existsByEventKey(, .findByEventKey(, String
Community 6: AuthService, AuthService.java, .buildIssuedTokens(, .invalidCredentials(, .login(, .refresh(, .validate(
Community 7: AuthController, AuthController.java, .extractBearerToken(, .login(, .refresh(, .validate(
Community 8: AuthServiceTest, AuthServiceTest.java, .setUp(, .testHappyPathLoginSuccess(, .testLoginExternalServiceError(, .testLoginFailureInvalidCredentials(
Community 9: BusinessException, BusinessException.java, .BusinessException(, .getCode(, RuntimeException
Community 10: AuthServiceApplication.java, AuthServiceApplication, .main(
Community 11: SecurityConfig.java, SecurityConfig, .passwordEncoder(
Community 12: Topics.java, Topics, .Topics(
Community 13: CacheProperties.java, CacheProperties
Community 14: JwtProperties.java, JwtProperties
Community 15: LoginRequest.java, toCommand(
Community 16: ProcessedEvent.java, ProcessedEvent
Community 17: ApiError.java
Community 18: CustomerRegisteredEvent.java
Community 19: LoginCommand.java
Community 20: RefreshCommand.java

## Instructions

Write a single JSON object mapping each community id (as a string) to its
2-5 word name to: c:\Users\semorale\proyectos\yanki-accounts-service\auth-service\.graphify\label-instructions\communities.json

Example:
```json
{
  "0": "Authentication Flow",
  "1": "Authentication Flow",
  "2": "Authentication Flow"
}
```

Then re-run `graphify update` (or `graphify label`) to ingest the names.
