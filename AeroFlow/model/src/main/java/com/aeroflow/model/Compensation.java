package com.aeroflow.model;

import com.aeroflow.model.enums.CompensationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compensation {
    private String passengerId;
    private CompensationType type;
    private Double value;
    private String qrCode;
    private LocalDateTime expiryDate;


}