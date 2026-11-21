# Node Description Batch 1 of 3

Graphify is running in assistant/skill mode (no API key). You are the host
assistant (Claude Code / Codex / Gemini CLI). Read the prompt below and write
your JSON answer to the answer file.

## Prompt

You are documenting nodes in a knowledge graph.
For each entry below, write ONE concise factual plain-language sentence
describing what it is or does. Use only the provided context.
For a code symbol (kind=code-symbol — a function, class, or constant),
describe what the function/symbol does based on its name, source location
and neighbors — e.g. "Resolves the configured ontology profile from graphify.yaml.".
Write every description in English (en). Do not switch languages.
No marketing language.
Respond ONLY with a JSON object mapping each node id (as a string) to its
one-sentence description — no prose, no markdown fences.

- "service_jwtservice_jwtservice": "JwtService" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/JwtService.java:L28 | neighbors=[JwtService.java, .buildSigningKey(), .extractRoles(), .generateAccessToken(), .generateRefreshToken(), .getAccessTokenExpirationSeconds()]
- "service_jwtservicetest_jwtservicetest": "JwtServiceTest" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/JwtServiceTest.java:L21 | neighbors=[JwtServiceTest.java, .credential(), .setUp(), .shouldIncludeExpectedClaimsInGenerated…, .shouldIssueRefreshTokenWithRefreshType…, .shouldRejectAccessTokenWhenRefreshToke…]
- "service_authservicetest_authservicetest": "AuthServiceTest" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/AuthServiceTest.java:L26 | neighbors=[AuthServiceTest.java, .credential(), .shouldIssueNewAccessTokenWhenRefreshTo…, .shouldIssueTokensWhenCredentialsAreVal…, .shouldRejectLoginWhenCredentialIsDisab…, .shouldRejectLoginWhenPasswordIsWrong()]
- "rest_globalexceptionhandler_globalexceptionhandler": "GlobalExceptionHandler" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/GlobalExceptionHandler.java:L24 | neighbors=[GlobalExceptionHandler.java, .buildResponse(), .handleBusinessException(), .handleDuplicateKeyException(), .handleInputException(), .handleUnexpectedException()]
- "service_jwtservicetest_jwtservicetest_credential": ".credential()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/JwtServiceTest.java:L119 | neighbors=[JwtServiceTest, .shouldIncludeExpectedClaimsInGenerated…, .shouldIssueRefreshTokenWithRefreshType…, .shouldRejectAccessTokenWhenRefreshToke…, .shouldRejectExpiredToken(), .shouldRejectTokenWithInvalidSignature()]
- "persistence_reactiveadapter_reactiveadapter": "ReactiveAdapter" | kind=code-symbol | source=src/main/java/com/yanki/auth/infrastructure/persistence/ReactiveAdapter.java:L17 | neighbors=[ReactiveAdapter.java, .ReactiveAdapter(), .toCompletable(), .toFlowable(), .toMaybe(), .toSingle()]
- "repository_processedeventrepository_processedeventrepository": "ProcessedEventRepository" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/repository/ProcessedEventRepository.java:L12 | neighbors=[ProcessedEventRepository.java, ProcessedEvent, ReactiveMongoRepository, .existsByEventKey(), .findByEventKey(), String]
- "rest_globalexceptionhandler_globalexceptionhandler_buildresponse": ".buildResponse()" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/GlobalExceptionHandler.java:L110 | neighbors=[GlobalExceptionHandler, .handleBusinessException(), .handleDuplicateKeyException(), .handleInputException(), .handleUnexpectedException(), .handleValidationException()]
- "service_authservice_authservice": "AuthService" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/AuthService.java:L22 | neighbors=[AuthService.java, .buildIssuedTokens(), .invalidCredentials(), .login(), .refresh(), .validate()]
- "controller_authcontroller_authcontroller": "AuthController" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/controller/AuthController.java:L30 | neighbors=[AuthController.java, .extractBearerToken(), .login(), .refresh(), .validate()]
- "service_authservicetest_authservicetest_credential": ".credential()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/AuthServiceTest.java:L161 | neighbors=[AuthServiceTest, .shouldIssueNewAccessTokenWhenRefreshTo…, .shouldIssueTokensWhenCredentialsAreVal…, .shouldRejectLoginWhenCredentialIsDisab…, .shouldRejectLoginWhenPasswordIsWrong()]
- "tests_authservicetest_authservicetest": "AuthServiceTest" | kind=code-symbol | source=tests/AuthServiceTest.java:L10 | neighbors=[AuthServiceTest.java, .setUp(), .testHappyPathLoginSuccess(), .testLoginExternalServiceError(), .testLoginFailureInvalidCredentials()]
- "exception_businessexception_businessexception": "BusinessException" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/exception/BusinessException.java:L9 | neighbors=[BusinessException.java, .BusinessException(), .getCode(), RuntimeException]
- "repository_processedeventrepository": "ProcessedEventRepository.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/repository/ProcessedEventRepository.java:L1 | neighbors=[ProcessedEvent, ReactiveMongoRepository, ProcessedEventRepository]
- "rest_globalexceptionhandler_globalexceptionhandler_handlebusinessexception": ".handleBusinessException()" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/GlobalExceptionHandler.java:L35 | neighbors=[GlobalExceptionHandler, .buildResponse(), .resolveStatus()]
- "service_jwtservice_jwtservice_parsetoken": ".parseToken()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/JwtService.java:L118 | neighbors=[JwtService, .extractRoles(), .toInstant()]
- "auth_authserviceapplication_authserviceapplication": "AuthServiceApplication" | kind=code-symbol | source=src/main/java/com/yanki/auth/AuthServiceApplication.java:L15 | neighbors=[AuthServiceApplication.java, .main()]
- "config_securityconfig_securityconfig": "SecurityConfig" | kind=code-symbol | source=src/main/java/com/yanki/auth/infrastructure/config/SecurityConfig.java:L15 | neighbors=[SecurityConfig.java, .passwordEncoder()]
- "controller_authcontroller_authcontroller_extractbearertoken": ".extractBearerToken()" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/controller/AuthController.java:L89 | neighbors=[AuthController, .validate()]
- "controller_authcontroller_authcontroller_validate": ".validate()" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/controller/AuthController.java:L75 | neighbors=[AuthController, .extractBearerToken()]
- "event_topics_topics": "Topics" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/event/Topics.java:L6 | neighbors=[Topics.java, .Topics()]
- "processedevent": "ProcessedEvent" | kind=code-symbol | neighbors=[ProcessedEventRepository.java, ProcessedEventRepository]
- "reactivemongorepository": "ReactiveMongoRepository" | kind=code-symbol | neighbors=[ProcessedEventRepository.java, ProcessedEventRepository]
- "rest_globalexceptionhandler_globalexceptionhandler_handleduplicatekeyexception": ".handleDuplicateKeyException()" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/GlobalExceptionHandler.java:L81 | neighbors=[GlobalExceptionHandler, .buildResponse()]
- "rest_globalexceptionhandler_globalexceptionhandler_handleinputexception": ".handleInputException()" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/GlobalExceptionHandler.java:L68 | neighbors=[GlobalExceptionHandler, .buildResponse()]
- "rest_globalexceptionhandler_globalexceptionhandler_handleunexpectedexception": ".handleUnexpectedException()" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/GlobalExceptionHandler.java:L94 | neighbors=[GlobalExceptionHandler, .buildResponse()]
- "rest_globalexceptionhandler_globalexceptionhandler_handlevalidationexception": ".handleValidationException()" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/GlobalExceptionHandler.java:L53 | neighbors=[GlobalExceptionHandler, .buildResponse()]
- "rest_globalexceptionhandler_globalexceptionhandler_resolvestatus": ".resolveStatus()" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/GlobalExceptionHandler.java:L129 | neighbors=[GlobalExceptionHandler, .handleBusinessException()]
- "service_authservice_authservice_invalidcredentials": ".invalidCredentials()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/AuthService.java:L116 | neighbors=[AuthService, .login()]
- "service_authservice_authservice_login": ".login()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/AuthService.java:L45 | neighbors=[AuthService, .invalidCredentials()]
- "service_authservicetest_authservicetest_shouldissuenewaccesstokenwhenrefreshtokenisvalid": ".shouldIssueNewAccessTokenWhenRefreshTokenIsValid()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/AuthServiceTest.java:L111 | neighbors=[AuthServiceTest, .credential()]
- "service_authservicetest_authservicetest_shouldissuetokenswhencredentialsarevalid": ".shouldIssueTokensWhenCredentialsAreValid()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/AuthServiceTest.java:L49 | neighbors=[AuthServiceTest, .credential()]
- "service_authservicetest_authservicetest_shouldrejectloginwhencredentialisdisabled": ".shouldRejectLoginWhenCredentialIsDisabled()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/AuthServiceTest.java:L97 | neighbors=[AuthServiceTest, .credential()]
- "service_authservicetest_authservicetest_shouldrejectloginwhenpasswordiswrong": ".shouldRejectLoginWhenPasswordIsWrong()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/AuthServiceTest.java:L71 | neighbors=[AuthServiceTest, .credential()]
- "service_jwtservice_jwtservice_buildsigningkey": ".buildSigningKey()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/JwtService.java:L193 | neighbors=[JwtService, .JwtService()]
- "service_jwtservice_jwtservice_extractroles": ".extractRoles()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/JwtService.java:L169 | neighbors=[JwtService, .parseToken()]
- "service_jwtservice_jwtservice_jwtservice": ".JwtService()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/JwtService.java:L61 | neighbors=[JwtService, .buildSigningKey()]
- "service_jwtservice_jwtservice_toinstant": ".toInstant()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/JwtService.java:L183 | neighbors=[JwtService, .parseToken()]
- "service_jwtservicetest_jwtservicetest_shouldincludeexpectedclaimsingeneratedaccesstoken": ".shouldIncludeExpectedClaimsInGeneratedAccessToken()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/JwtServiceTest.java:L43 | neighbors=[JwtServiceTest, .credential()]
- "service_jwtservicetest_jwtservicetest_shouldissuerefreshtokenwithrefreshtype": ".shouldIssueRefreshTokenWithRefreshType()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/JwtServiceTest.java:L65 | neighbors=[JwtServiceTest, .credential()]

## Instructions

Write a single JSON object mapping each node id to a one-sentence description
to: c:\Users\semorale\proyectos\yanki-accounts-service\auth-service\.graphify\description-instructions\batch-000.json

Keep each description factual and concise (one sentence). No markdown, no prose
outside the JSON object. It is acceptable to omit a node if context is
insufficient — but include every node you can ground confidently.

Example answer format:
```json
{
  "node_id_1": "Resolves the configured ontology profile from graphify.yaml.",
  "node_id_2": "Colonel James Barclay, an antagonist in The Crooked Man."
}
```
