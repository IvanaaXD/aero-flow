package com.aeroflow.service.services;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    @Override
    public void run(String... args) {
        System.out.println("✅ Sistem AeroFlow spreman. Čekam tvoju akciju u UI...");
    }
}