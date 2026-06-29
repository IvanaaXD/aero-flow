package com.aeroflow.service.repositories;

import com.aeroflow.model.Baggage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaggageRepository extends JpaRepository<Baggage, String> {}