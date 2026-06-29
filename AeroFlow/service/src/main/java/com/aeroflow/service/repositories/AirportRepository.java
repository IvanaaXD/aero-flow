package com.aeroflow.service.repositories;

import com.aeroflow.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportRepository extends JpaRepository<Airport, String> {}