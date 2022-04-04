package com.example.MyURLShortener.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Url {
    private String url;
    private LocalDateTime created;
}
