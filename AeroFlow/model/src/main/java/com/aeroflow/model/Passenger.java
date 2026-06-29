package com.aeroflow.model;

import com.aeroflow.model.enums.LoyaltyTier;
import com.aeroflow.model.enums.PassengerStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Passenger extends User {

    public String getPassengerId() {
        return this.getUserId();
    }

    @Enumerated(EnumType.STRING)
    private LoyaltyTier loyaltyTier;

    @Enumerated(EnumType.STRING)
    private PassengerStatus currentStatus;

    public Passenger() { super(); }

    public Passenger(String passengerId, String name, PassengerStatus currentStatus) {
        super(passengerId, name, null, null);
        this.currentStatus = currentStatus;
    }

    public Passenger(String userId, String name, String email, String password, String passengerId, LoyaltyTier loyaltyTier, PassengerStatus currentStatus) {
        super(userId, name, email, password);
        this.loyaltyTier = loyaltyTier;
        this.currentStatus = currentStatus;
    }
}