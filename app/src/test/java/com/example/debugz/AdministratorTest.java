package com.example.debugz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.debugz.models.Administrator;

import org.junit.Before;
import org.junit.Test;

/**
 * Verifies the Administrator model's public API: construction, field mutation.
 * The Administrator model is an intentionally minimal identity holder; permission
 * enforcement and Firestore operations live outside the model class.
 */
public class AdministratorTest {

    private Administrator admin;

    @Before
    public void setUp() {
        admin = new Administrator("admin_001", "Platform Admin", "admin@lums.edu.pk");
    }

    // ──────────────────────────────────────────────
    // Constructor Tests
    // ──────────────────────────────────────────────

    @Test
    public void testParameterizedConstructor_setsAllFields() {
        assertEquals("admin_001", admin.getAdminId());
        assertEquals("Platform Admin", admin.getName());
        assertEquals("admin@lums.edu.pk", admin.getEmail());
    }

    @Test
    public void testDefaultConstructor_fieldsAreNull() {
        Administrator defaultAdmin = new Administrator();
        assertNull(defaultAdmin.getAdminId());
        assertNull(defaultAdmin.getName());
        assertNull(defaultAdmin.getEmail());
    }

    // ──────────────────────────────────────────────
    // Getter / Setter Tests
    // ──────────────────────────────────────────────

    @Test
    public void testSetAndGetAdminId() {
        admin.setAdminId("admin_999");
        assertEquals("admin_999", admin.getAdminId());
    }

    @Test
    public void testSetAndGetName() {
        admin.setName("Super Admin");
        assertEquals("Super Admin", admin.getName());
    }

    @Test
    public void testSetAndGetEmail() {
        admin.setEmail("superadmin@lums.edu.pk");
        assertEquals("superadmin@lums.edu.pk", admin.getEmail());
    }

    // ──────────────────────────────────────────────
    // Realistic Scenario Tests (US15)
    // ──────────────────────────────────────────────

    /**
     * Verifies that an admin instance can be constructed with the demo ID used by AdminDashboardActivity.
     */
    @Test
    public void testRealisticAdmin_demoAccount() {
        Administrator demoAdmin = new Administrator(
                "demo_admin_001",
                "Demo Admin",
                "admin@debugz.app"
        );
        assertEquals("demo_admin_001", demoAdmin.getAdminId());
        assertEquals("Demo Admin", demoAdmin.getName());
    }
}

