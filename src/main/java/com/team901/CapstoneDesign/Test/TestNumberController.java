package com.team901.CapstoneDesign.Test;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestNumberController {

    private final TestNumberRepository repository;

    public TestNumberController(TestNumberRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/save")
    public String saveNumber(@RequestParam Integer number) {
        TestNumber testNumber = new TestNumber();
        testNumber.setNumber(number);
        repository.save(testNumber);
        return "Saved number: " + number;
    }

    @GetMapping("/latest")
    public Integer getLatestNumber() {
        return repository.findAll()
                .stream()
                .reduce((first, second) -> second) // 가장 마지막 항목
                .map(TestNumber::getNumber)
                .orElse(null);
    }
}
