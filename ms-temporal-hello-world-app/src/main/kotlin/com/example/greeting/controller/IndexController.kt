package com.example.greeting.controller

import com.example.greeting.service.GreetingService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class IndexController(val greetingService: GreetingService) {

    @GetMapping("/")
    fun index(): String {
        return "Hello World"
    }

    @GetMapping("/greet")
    fun greet(@RequestParam(name="name") name: String): String {
        return "Hello, " + greetingService.greet(name)
    }
}