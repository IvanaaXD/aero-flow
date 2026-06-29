package com.aeroflow.service.repositories;

import com.aeroflow.model.AirlineAgent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirlineAgentRepository extends JpaRepository<AirlineAgent, String> {}