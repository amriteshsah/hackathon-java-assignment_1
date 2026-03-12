package com.example.newproject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    @Test
    void shouldGreetUserByName() {
        App app = new App();
        assertEquals("Hello, Codex!", app.greet("Codex"));
    }
}
