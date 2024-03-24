package org.avalancs.template

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class StringTemplateTest {
    StringTemplate t;

    @BeforeEach
    void setUp() {
        t = new StringTemplate();
    }

    @Test
    void testApplyTemplate_String() {
        String output = t.apply('<p>{{something}}</p>', [something: "Test"]);
        println(output);
        assertEquals('<p>Test</p>', output);
    }
}
