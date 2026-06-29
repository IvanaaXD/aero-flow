package com.aeroflow.service.repositories;

import com.aeroflow.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerRepository extends JpaRepository<Passenger, String> {}