package com.example.debugz;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.debugz.models.Account;

import org.junit.Before;
import org.junit.Test;

/**
 * Verifies the Account model used for manual signup + admin approval.
 */
public class AccountTest {

    private Account account;

    @Before
    public void setUp() {
        account = new Account(
                "27100001",
                "Ali Raza",
                "ali@lums.edu.pk",
                "pass1234",
                "STUDENT",
                Account.STATUS_PENDING,
                1712500000000L
        );
    }

    @Test
    public void testParameterizedConstructor_setsAllFields() {
        assertEquals("27100001", account.getAccountId());
        assertEquals("Ali Raza", account.getName());
        assertEquals("ali@lums.edu.pk", account.getEmail());
        assertEquals("pass1234", account.getPassword());
        assertEquals("STUDENT", account.getRole());
        assertEquals(Account.STATUS_PENDING, account.getStatus());
        assertEquals(1712500000000L, account.getCreatedAt());
        assertNotNull(account.getFriendIds());
        assertTrue(account.getFriendIds().isEmpty());
    }

    @Test
    public void testDefaultConstructor_fieldsAreNullOrZero() {
        Account empty = new Account();
        assertNull(empty.getAccountId());
        assertNull(empty.getName());
        assertNull(empty.getEmail());
        assertNull(empty.getPassword());
        assertNull(empty.getRole());
        assertNull(empty.getStatus());
        assertEquals(0L, empty.getCreatedAt());
    }

    @Test
    public void testFriendIds_initializedEmpty() {
        Account a = new Account();
        assertNotNull(a.getFriendIds());
        assertTrue(a.getFriendIds().isEmpty());
        assertNotNull(a.getFriendRequests());
        assertTrue(a.getFriendRequests().isEmpty());
    }

    @Test
    public void testPendingStatusHelper() {
        assertTrue(account.isPending());
        assertFalse(account.isApproved());
        assertFalse(account.isRejected());
    }

    @Test
    public void testApprovedStatusHelper() {
        account.setStatus(Account.STATUS_APPROVED);
        assertTrue(account.isApproved());
        assertFalse(account.isPending());
        assertFalse(account.isRejected());
    }

    @Test
    public void testRejectedStatusHelper() {
        account.setStatus(Account.STATUS_REJECTED);
        assertTrue(account.isRejected());
        assertFalse(account.isPending());
        assertFalse(account.isApproved());
    }

    @Test
    public void testSettersAndGetters() {
        account.setAccountId("org_css");
        account.setName("CS Society");
        account.setEmail("css@lums.edu.pk");
        account.setPassword("secret");
        account.setRole("ORGANIZER");
        account.setStatus(Account.STATUS_APPROVED);
        account.setCreatedAt(999L);

        assertEquals("org_css", account.getAccountId());
        assertEquals("CS Society", account.getName());
        assertEquals("css@lums.edu.pk", account.getEmail());
        assertEquals("secret", account.getPassword());
        assertEquals("ORGANIZER", account.getRole());
        assertEquals(Account.STATUS_APPROVED, account.getStatus());
        assertEquals(999L, account.getCreatedAt());
    }
}

