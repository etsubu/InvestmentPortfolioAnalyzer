package com.etsubu.portfoliotracker.API.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class ErrorEntry {
    private final String code;
    private final String description;
}
