package com.iteleme.backend.config;

import com.iteleme.backend.constant.UserRole;
import com.iteleme.backend.context.LoginUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class JwtUtilEnumTest {

    @Test
    void shouldRoundTripEnumRoleThroughNumericJwtClaim() {
        JwtUtil jwtUtil = new JwtUtil("0123456789abcdef0123456789abcdef", 1);

        String token = jwtUtil.create(10001, UserRole.ADMIN);
        LoginUser loginUser = jwtUtil.parse(token);

        assertNotNull(token);
        assertEquals(10001, loginUser.userId());
        assertSame(UserRole.ADMIN, loginUser.role());
        assertEquals(token, loginUser.token());
    }

    @Test
    void shouldUseCustomerRoleByDefault() {
        JwtUtil jwtUtil = new JwtUtil("0123456789abcdef0123456789abcdef", 1);

        LoginUser loginUser = jwtUtil.parse(jwtUtil.create(10002));

        assertSame(UserRole.CUSTOMER, loginUser.role());
    }
}
