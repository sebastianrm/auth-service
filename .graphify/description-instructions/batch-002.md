# Node Description Batch 3 of 3

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

- "service_authservicetest_authservicetest_shouldrejectrefreshwhentokenisinvalid": ".shouldRejectRefreshWhenTokenIsInvalid()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/AuthServiceTest.java:L132 | neighbors=[AuthServiceTest]
- "service_authservicetest_authservicetest_shouldreturnclaimswhenaccesstokenisvalid": ".shouldReturnClaimsWhenAccessTokenIsValid()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/AuthServiceTest.java:L145 | neighbors=[AuthServiceTest]
- "service_jwtservice": "JwtService.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/JwtService.java:L1 | neighbors=[JwtService]
- "service_jwtservice_jwtservice_generateaccesstoken": ".generateAccessToken()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/JwtService.java:L72 | neighbors=[JwtService]
- "service_jwtservice_jwtservice_generaterefreshtoken": ".generateRefreshToken()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/JwtService.java:L94 | neighbors=[JwtService]
- "service_jwtservice_jwtservice_getaccesstokenexpirationseconds": ".getAccessTokenExpirationSeconds()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/JwtService.java:L150 | neighbors=[JwtService]
- "service_jwtservice_jwtservice_getrefreshtokenexpirationseconds": ".getRefreshTokenExpirationSeconds()" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/service/JwtService.java:L159 | neighbors=[JwtService]
- "service_jwtservicetest": "JwtServiceTest.java" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/JwtServiceTest.java:L1 | neighbors=[JwtServiceTest]
- "service_jwtservicetest_jwtservicetest_setup": ".setUp()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/JwtServiceTest.java:L33 | neighbors=[JwtServiceTest]
- "service_jwtservicetest_jwtservicetest_shouldrejectsecretshorterthanthirtytwobytes": ".shouldRejectSecretShorterThanThirtyTwoBytes()" | kind=code-symbol | source=src/test/java/com/yanki/auth/domain/service/JwtServiceTest.java:L111 | neighbors=[JwtServiceTest]
- "string": "String" | kind=code-symbol | neighbors=[ProcessedEventRepository]
- "tests_authservicetest": "AuthServiceTest.java" | kind=code-symbol | source=tests/AuthServiceTest.java:L1 | neighbors=[AuthServiceTest]
- "tests_authservicetest_authservicetest_setup": ".setUp()" | kind=code-symbol | source=tests/AuthServiceTest.java:L19 | neighbors=[AuthServiceTest]
- "tests_authservicetest_authservicetest_testhappypathloginsuccess": ".testHappyPathLoginSuccess()" | kind=code-symbol | source=tests/AuthServiceTest.java:L24 | neighbors=[AuthServiceTest]
- "tests_authservicetest_authservicetest_testloginexternalserviceerror": ".testLoginExternalServiceError()" | kind=code-symbol | source=tests/AuthServiceTest.java:L45 | neighbors=[AuthServiceTest]
- "tests_authservicetest_authservicetest_testloginfailureinvalidcredentials": ".testLoginFailureInvalidCredentials()" | kind=code-symbol | source=tests/AuthServiceTest.java:L36 | neighbors=[AuthServiceTest]
- "dto_apierror": "ApiError.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/interfaces/rest/dto/ApiError.java:L1
- "event_customerregisteredevent": "CustomerRegisteredEvent.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/event/CustomerRegisteredEvent.java:L1
- "model_logincommand": "LoginCommand.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/model/LoginCommand.java:L1
- "model_refreshcommand": "RefreshCommand.java" | kind=code-symbol | source=src/main/java/com/yanki/auth/domain/model/RefreshCommand.java:L1

## Instructions

Write a single JSON object mapping each node id to a one-sentence description
to: c:\Users\semorale\proyectos\yanki-accounts-service\auth-service\.graphify\description-instructions\batch-002.json

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
