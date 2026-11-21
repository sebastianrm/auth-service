# Node Description Batch 2 of 3

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

- "service_jwtservicetest_jwtservicetest_shouldrejectaccesstokenwhenrefreshtokenisexpected": ".shouldRejectAccessTokenWhenRefreshTokenIsExpected()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/JwtServiceTest.java:L101 | neighbors=[JwtServiceTest, .credential()]
- "service_jwtservicetest_jwtservicetest_shouldrejectexpiredtoken": ".shouldRejectExpiredToken()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/JwtServiceTest.java:L76 | neighbors=[JwtServiceTest, .credential()]
- "service_jwtservicetest_jwtservicetest_shouldrejecttokenwithinvalidsignature": ".shouldRejectTokenWithInvalidSignature()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/JwtServiceTest.java:L87 | neighbors=[JwtServiceTest, .credential()]
- "service_jwtservicetest_jwtservicetest_shouldsigntokenswithhs256algorithm": ".shouldSignTokensWithHs256Algorithm()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/JwtServiceTest.java:L56 | neighbors=[JwtServiceTest, .credential()]
- "auth_authserviceapplication": "AuthServiceApplication.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/AuthServiceApplication.java:L1 | neighbors=[AuthServiceApplication]
- "auth_authserviceapplication_authserviceapplication_main": ".main()" | kind=code-symbol | source=src/main/java/com/yanki/auth/AuthServiceApplication.java:L23 | neighbors=[AuthServiceApplication]
- "config_cacheproperties": "CacheProperties.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/infrastructure/config/CacheProperties.java:L1 | neighbors=[CacheProperties]
- "config_cacheproperties_cacheproperties": "CacheProperties" | kind=code-symbol | source=src/main/java/com/yanki/auth/infrastructure/config/CacheProperties.java:L15 | neighbors=[CacheProperties.java]
- "config_jwtproperties": "JwtProperties.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/infrastructure/config/JwtProperties.java:L1 | neighbors=[JwtProperties]
- "config_jwtproperties_jwtproperties": "JwtProperties" | kind=code-symbol | source=src/main/java/com/yanki/auth/infrastructure/config/JwtProperties.java:L16 | neighbors=[JwtProperties.java]
- "config_securityconfig": "SecurityConfig.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/infrastructure/config/SecurityConfig.java:L1 | neighbors=[SecurityConfig]
- "config_securityconfig_securityconfig_passwordencoder": ".passwordEncoder()" | kind=code-symbol | source=src/main/java/com/yanki/auth/infrastructure/config/SecurityConfig.java:L23 | neighbors=[SecurityConfig]
- "controller_authcontroller": "AuthController.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/controller/AuthController.java:L1 | neighbors=[AuthController]
- "controller_authcontroller_authcontroller_login": ".login()" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/controller/AuthController.java:L49 | neighbors=[AuthController]
- "controller_authcontroller_authcontroller_refresh": ".refresh()" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/controller/AuthController.java:L62 | neighbors=[AuthController]
- "dto_loginrequest": "LoginRequest.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/dto/LoginRequest.java:L1 | neighbors=[toCommand()]
- "dto_loginrequest_tocommand": "toCommand()" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/dto/LoginRequest.java:L23 | neighbors=[LoginRequest.java]
- "event_topics": "Topics.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/event/Topics.java:L1 | neighbors=[Topics]
- "event_topics_topics_topics": ".Topics()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/event/Topics.java:L14 | neighbors=[Topics]
- "exception_businessexception": "BusinessException.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/exception/BusinessException.java:L1 | neighbors=[BusinessException]
- "exception_businessexception_businessexception_businessexception": ".BusinessException()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/exception/BusinessException.java:L20 | neighbors=[BusinessException]
- "exception_businessexception_businessexception_getcode": ".getCode()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/exception/BusinessException.java:L30 | neighbors=[BusinessException]
- "model_processedevent": "ProcessedEvent.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/model/ProcessedEvent.java:L1 | neighbors=[ProcessedEvent]
- "model_processedevent_processedevent": "ProcessedEvent" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/model/ProcessedEvent.java:L21 | neighbors=[ProcessedEvent.java]
- "persistence_reactiveadapter": "ReactiveAdapter.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/infrastructure/persistence/ReactiveAdapter.java:L1 | neighbors=[ReactiveAdapter]
- "persistence_reactiveadapter_reactiveadapter_reactiveadapter": ".ReactiveAdapter()" | kind=code-symbol | source=src/main/java/com/yanki/auth/infrastructure/persistence/ReactiveAdapter.java:L22 | neighbors=[ReactiveAdapter]
- "persistence_reactiveadapter_reactiveadapter_tocompletable": ".toCompletable()" | kind=code-symbol | source=src/main/java/com/yanki/auth/infrastructure/persistence/ReactiveAdapter.java:L64 | neighbors=[ReactiveAdapter]
- "persistence_reactiveadapter_reactiveadapter_toflowable": ".toFlowable()" | kind=code-symbol | source=src/main/java/com/yanki/auth/infrastructure/persistence/ReactiveAdapter.java:L54 | neighbors=[ReactiveAdapter]
- "persistence_reactiveadapter_reactiveadapter_tomaybe": ".toMaybe()" | kind=code-symbol | source=src/main/java/com/yanki/auth/infrastructure/persistence/ReactiveAdapter.java:L43 | neighbors=[ReactiveAdapter]
- "persistence_reactiveadapter_reactiveadapter_tosingle": ".toSingle()" | kind=code-symbol | source=src/main/java/com/yanki/auth/infrastructure/persistence/ReactiveAdapter.java:L32 | neighbors=[ReactiveAdapter]
- "repository_processedeventrepository_processedeventrepository_existsbyeventkey": ".existsByEventKey()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/repository/ProcessedEventRepository.java:L28 | neighbors=[ProcessedEventRepository]
- "repository_processedeventrepository_processedeventrepository_findbyeventkey": ".findByEventKey()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/repository/ProcessedEventRepository.java:L20 | neighbors=[ProcessedEventRepository]
- "rest_globalexceptionhandler": "GlobalExceptionHandler.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/GlobalExceptionHandler.java:L1 | neighbors=[GlobalExceptionHandler]
- "runtimeexception": "RuntimeException" | kind=code-symbol | neighbors=[BusinessException]
- "service_authservice": "AuthService.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/AuthService.java:L1 | neighbors=[AuthService]
- "service_authservice_authservice_buildissuedtokens": ".buildIssuedTokens()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/AuthService.java:L101 | neighbors=[AuthService]
- "service_authservice_authservice_refresh": ".refresh()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/AuthService.java:L65 | neighbors=[AuthService]
- "service_authservice_authservice_validate": ".validate()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/AuthService.java:L88 | neighbors=[AuthService]
- "service_authservicetest": "AuthServiceTest.java" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/AuthServiceTest.java:L1 | neighbors=[AuthServiceTest]
- "service_authservicetest_authservicetest_shouldrejectloginwhenuserdoesnotexist": ".shouldRejectLoginWhenUserDoesNotExist()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/AuthServiceTest.java:L85 | neighbors=[AuthServiceTest]

## Instructions

Write a single JSON object mapping each node id to a one-sentence description
to: c:\Users\semorale\proyectos\yanki-accounts-service\auth-service\.graphify\description-instructions\batch-001.json

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
