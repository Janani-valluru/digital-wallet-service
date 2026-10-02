package com.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserAccountRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 12, max = 100)
    private String password;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal initialBalance = BigDecimal.ZERO;
}
