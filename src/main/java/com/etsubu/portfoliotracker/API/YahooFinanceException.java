package com.etsubu.portfoliotracker.API;

import com.etsubu.portfoliotracker.API.Model.ErrorEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class YahooFinanceException extends Exception{
    private final int code;
    private final ErrorEntry error;
}
