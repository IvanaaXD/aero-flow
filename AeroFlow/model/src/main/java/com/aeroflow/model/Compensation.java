package com.aeroflow.model;

import com.aeroflow.model.enums.CompensationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compensation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;

    @Enumerated(EnumType.STRING)
    private CompensationType type;

    @Column(name = "amount")
    private Double value;

    private String qrCode;
    private LocalDateTime expiryDate;

    private String message;

    public String getPassengerId() {
        return (this.passenger != null) ? this.passenger.getPassengerId() : null;
    }
}