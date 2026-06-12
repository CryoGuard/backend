package com.example.cryoguard.evaluation.interfaces.acl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ContainerQueryService - cross-BC ACL interface for container queries.
 * T2.6 - Add ContainerQueryService interface in evaluation/interfaces/acl/
 *
 * This interface is consumed by evaluation BC to query container information
 * from the monitoring BC (boxId lookup for alerts).
 */
class EvaluationContainerQueryServiceTest {

    @Test
    @DisplayName("should have getCode method that returns String")
    void shouldHaveGetCodeMethodThatReturnsString() {
        // GIVEN ContainerQueryService interface
        // WHEN checking getCode method signature
        // THEN it should accept Long and return String
        assertTrue(hasGetCodeMethod(ContainerQueryService.class));
    }

    @Test
    @DisplayName("getCode should accept Long parameter")
    void getCodeShouldAcceptLongParameter() {
        // GIVEN ContainerQueryService interface
        // WHEN inspecting getCode method
        // THEN parameter type should be Long
        Class<?> paramType = getGetCodeParameterType(ContainerQueryService.class);
        assertEquals(Long.class, paramType);
    }

    @Test
    @DisplayName("getCode should return String")
    void getCodeShouldReturnString() {
        // GIVEN ContainerQueryService interface
        // WHEN inspecting getCode method
        // THEN return type should be String
        Class<?> returnType = getGetCodeReturnType(ContainerQueryService.class);
        assertEquals(String.class, returnType);
    }

    @Test
    @DisplayName("interface should be public")
    void interfaceShouldBePublic() {
        // GIVEN ContainerQueryService interface
        // THEN it should be public
        assertTrue(ContainerQueryService.class.isInterface());
        assertTrue(java.lang.reflect.Modifier.isPublic(ContainerQueryService.class.getModifiers()));
    }

    // Helper methods to inspect interface via reflection
    private boolean hasGetCodeMethod(Class<?> clazz) {
        for (var method : clazz.getMethods()) {
            if ("getCode".equals(method.getName()) && method.getParameterCount() == 1) {
                return true;
            }
        }
        return false;
    }

    private Class<?> getGetCodeParameterType(Class<?> clazz) {
        for (var method : clazz.getMethods()) {
            if ("getCode".equals(method.getName()) && method.getParameterCount() == 1) {
                return method.getParameterTypes()[0];
            }
        }
        throw new AssertionError("getCode method not found");
    }

    private Class<?> getGetCodeReturnType(Class<?> clazz) {
        for (var method : clazz.getMethods()) {
            if ("getCode".equals(method.getName()) && method.getParameterCount() == 1) {
                return method.getReturnType();
            }
        }
        throw new AssertionError("getCode method not found");
    }
}