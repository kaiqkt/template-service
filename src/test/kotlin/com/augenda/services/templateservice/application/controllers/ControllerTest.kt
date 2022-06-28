package com.augenda.services.templateservice.application.controllers

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ControllerTest {
    private val controller = Controller()

    @Test
    fun `given request return hello world and status 200`() {
        val response = controller.helloWorld()

        Assertions.assertEquals("Hello World", response.body)
    }
}