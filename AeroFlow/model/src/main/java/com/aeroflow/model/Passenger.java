package com.aeroflow.model;

import com.aeroflow.model.enums.LoyaltyTier;
import com.aeroflow.model.enums.PassengerStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Passenger extends User {
    private String passengerId;
    private LoyaltyTier loyaltyTier;
    private PassengerStatus currentStatus;

    public Passenger() {
        super();
    }

    public Passenger(String passengerId, String name, PassengerStatus currentStatus) {
        super(null, name, null, null);
        this.passengerId = passengerId;
        this.currentStatus = currentStatus;
    }

    public Passenger(String userId, String name, String email, String password, String passengerId, LoyaltyTier loyaltyTier, PassengerStatus currentStatus) {
        super(userId, name, email, password);
        this.passengerId = passengerId;
        this.loyaltyTier = loyaltyTier;
        this.currentStatus = currentStatus;
    }
}