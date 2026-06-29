package com.aeroflow.service.repositories;

import com.aeroflow.model.Compensation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompensationRepository extends JpaRepository<Compensation, Long> {}