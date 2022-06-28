package com.augenda.services.templateservice.application.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller : HelloworldApi {

    override fun helloWorld(): ResponseEntity<String> {
        return ResponseEntity("Hello World", HttpStatus.OK)
    }
}