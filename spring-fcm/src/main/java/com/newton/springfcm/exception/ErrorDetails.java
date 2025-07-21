package com.newton.springfcm.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorDetails {
    LocalDateTime timestamp;
    String message;
    String details;
}
