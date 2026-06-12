# Testing Capabilities — CryoGuard

**Strict TDD Mode**: true  
**Detected**: 2026-06-11  
**Test Runner**: Maven (`./mvnw test`)

## Test Runner

- **Command**: `./mvnw test`
- **Framework**: JUnit 5 + Mockito
- **Build tool**: Maven 4.x with Spring Boot Maven plugin

## Test Layers

| Layer | Available | Tool |
|-------|-----------|------|
| Unit | ✅ | JUnit 5 + Mockito |
| Integration | ✅ (limited) | Spring Boot Test (`@SpringBootTest`, `MockMvc`, `WebApplicationFactory`) |
| E2E | ❌ | — |

## Coverage

- **Available**: ❌
- **Command**: —
- **Gap**: No JaCoCo or other coverage plugin configured

## Quality Tools

| Tool | Available | Command |
|------|-----------|---------|
| Linter | ❌ | — |
| Type checker | ❌ | — |
| Formatter | ❌ | — |

## Existing Tests

**Only 1 test file exists**:
- `src/test/java/com/example/cryoguard/CryoGuardApplicationTests.java` — smoke test (`@SpringBootTest` context load)

**Gap**: No per-context integration tests. Each bounded context (iam, monitoring, evaluation, logistics, etc.) lacks dedicated `@WebMvcTest` or `@DataJpaTest` tests.

## Recommendations

1. Add `jacoco-maven-plugin` for coverage reports
2. Add per-context integration tests using `@WebMvcTest` for controllers and `@DataJpaTest` for repositories
3. Consider Spotless or CheckStyle for formatter/linter enforcement
4. Add Spring Security test support with `@WithMockUser` for authenticated endpoint tests