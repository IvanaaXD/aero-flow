package com.aeroflow.service.repositories;

import com.aeroflow.model.AirportStaff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportStaffRepository extends JpaRepository<AirportStaff, String> {}