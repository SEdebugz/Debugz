package com.example.debugz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.debugz.models.Student;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for the Student model class.
 * Covers constructor initialization, getters/setters, preference management,
 * and registration tracking — relevant to US1 (browsing), US4 (RSVP), US10 (monitor RSVPs).
 */
public class StudentTest {

    private Student student;

    @Before
    public void setUp() {
        student = new Student("stu_001", "Faneez Ali", "faneez@lums.edu.pk", "SBASSE", "Sophomore");
    }

    // ──────────────────────────────────────────────
    // Constructor Tests
    // ──────────────────────────────────────────────

    @Test
    public void testParameterizedConstructor_setsAllFields() {
        assertEquals("stu_001", student.getStudentId());
        assertEquals("Faneez Ali", student.getName());
        assertEquals("faneez@lums.edu.pk", student.getEmail());
        assertEquals("SBASSE", student.getSchool());
        assertEquals("Sophomore", student.getYear());
    }

    @Test
    public void testParameterizedConstructor_initializesEmptyLists() {
        assertNotNull("Preferences list should not be null", student.getPreferences());
        assertTrue("Preferences list should start empty", student.getPreferences().isEmpty());
        assertNotNull("RegistrationIds list should not be null", student.getRegistrationIds());
        assertTrue("RegistrationIds list should start empty", student.getRegistrationIds().isEmpty());
    }

    @Test
    public void testDefaultConstructor_initializesEmptyLists() {
        Student defaultStudent = new Student();
        assertNotNull("Default preferences list should not be null", defaultStudent.getPreferences());
        assertTrue("Default preferences list should be empty", defaultStudent.getPreferences().isEmpty());
        assertNotNull("Default registrationIds list should not be null", defaultStudent.getRegistrationIds());
        assertTrue("Default registrationIds list should be empty", defaultStudent.getRegistrationIds().isEmpty());
    }

    @Test
    public void testDefaultConstructor_fieldsAreNull() {
        Student defaultStudent = new Student();
        assertNull(defaultStudent.getStudentId());
        assertNull(defaultStudent.getName());
        assertNull(defaultStudent.getEmail());
        assertNull(defaultStudent.getSchool());
        assertNull(defaultStudent.getYear());
    }

    // ──────────────────────────────────────────────
    // Getter / Setter Tests
    // ──────────────────────────────────────────────

    @Test
    public void testSetAndGetStudentId() {
        student.setStudentId("stu_999");
        assertEquals("stu_999", student.getStudentId());
    }

    @Test
    public void testSetAndGetName() {
        student.setName("Muhammad Ahmed");
        assertEquals("Muhammad Ahmed", student.getName());
    }

    @Test
    public void testSetAndGetEmail() {
        student.setEmail("ahmed@lums.edu.pk");
        assertEquals("ahmed@lums.edu.pk", student.getEmail());
    }

    @Test
    public void testSetAndGetSchool() {
        student.setSchool("SDSB");
        assertEquals("SDSB", student.getSchool());
    }

    @Test
    public void testSetAndGetYear() {
        student.setYear("Senior");
        assertEquals("Senior", student.getYear());
    }

    // ──────────────────────────────────────────────
    // Preference Management Tests (relates to US2 – search by interest)
    // ──────────────────────────────────────────────

    @Test
    public void testAddPreference_addsNewTag() {
        student.addPreference("sports");
        assertTrue(student.getPreferences().contains("sports"));
        assertEquals(1, student.getPreferences().size());
    }

    @Test
    public void testAddPreference_preventsDuplicates() {
        student.addPreference("clubs");
        student.addPreference("clubs");
        assertEquals("Should not add duplicate preference", 1, student.getPreferences().size());
    }

    @Test
    public void testAddPreference_multipleDistinctTags() {
        student.addPreference("sports");
        student.addPreference("talks");
        student.addPreference("performances");
        assertEquals(3, student.getPreferences().size());
        assertTrue(student.getPreferences().contains("talks"));
    }

    @Test
    public void testSetPreferences_replacesEntireList() {
        student.addPreference("old_tag");
        List<String> newPrefs = Arrays.asList("music", "tech", "debate");
        student.setPreferences(newPrefs);
        assertEquals(3, student.getPreferences().size());
        assertFalse(student.getPreferences().contains("old_tag"));
        assertTrue(student.getPreferences().contains("tech"));
    }

    // ──────────────────────────────────────────────
    // Registration Management Tests (relates to US4, US10)
    // ──────────────────────────────────────────────

    @Test
    public void testAddRegistration_addsNewId() {
        student.addRegistration("reg_001");
        assertTrue(student.getRegistrationIds().contains("reg_001"));
        assertEquals(1, student.getRegistrationIds().size());
    }

    @Test
    public void testAddRegistration_preventsDuplicates() {
        student.addRegistration("reg_001");
        student.addRegistration("reg_001");
        assertEquals("Should not add duplicate registration", 1, student.getRegistrationIds().size());
    }

    @Test
    public void testAddRegistration_multipleDistinctRegistrations() {
        student.addRegistration("reg_001");
        student.addRegistration("reg_002");
        student.addRegistration("reg_003");
        assertEquals(3, student.getRegistrationIds().size());
    }

    @Test
    public void testRemoveRegistration_removesExistingId() {
        student.addRegistration("reg_001");
        student.addRegistration("reg_002");
        student.removeRegistration("reg_001");
        assertFalse(student.getRegistrationIds().contains("reg_001"));
        assertEquals(1, student.getRegistrationIds().size());
    }

    @Test
    public void testRemoveRegistration_nonExistentId_doesNothing() {
        student.addRegistration("reg_001");
        student.removeRegistration("reg_999");
        assertEquals("Removing a non-existent ID should not change the list", 1, student.getRegistrationIds().size());
    }

    @Test
    public void testSetRegistrationIds_replacesEntireList() {
        student.addRegistration("old_reg");
        List<String> newRegs = Arrays.asList("reg_100", "reg_200");
        student.setRegistrationIds(newRegs);
        assertEquals(2, student.getRegistrationIds().size());
        assertFalse(student.getRegistrationIds().contains("old_reg"));
        assertTrue(student.getRegistrationIds().contains("reg_200"));
    }

    // ──────────────────────────────────────────────
    // Realistic Data Scenario Tests
    // ──────────────────────────────────────────────

    @Test
    public void testRealisticStudentProfile() {
        Student realStudent = new Student(
                "27100284",
                "Abdul Moeez Khurshid",
                "27100284@lums.edu.pk",
                "SBASSE",
                "Junior"
        );
        realStudent.addPreference("technology");
        realStudent.addPreference("debate");
        realStudent.addRegistration("reg_lumun_2026");
        realStudent.addRegistration("reg_career_fair");

        assertEquals("27100284", realStudent.getStudentId());
        assertEquals(2, realStudent.getPreferences().size());
        assertEquals(2, realStudent.getRegistrationIds().size());
        assertTrue(realStudent.getRegistrationIds().contains("reg_lumun_2026"));
    }
}


