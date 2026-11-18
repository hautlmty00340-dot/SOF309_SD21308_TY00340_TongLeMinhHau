package com.aubadminton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuBadmintonApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuBadmintonApplication.class, args);
        System.out.println("");
        System.out.println("🏸 ═══════════════════════════════════════════");
        System.out.println("   AU BADMINTON V2.8 — Server started!");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("🔌  API Base : http://localhost:8080/api");
        System.out.println("🗄️  Database : SQL Server (xác nhận tại /api/health)");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("");
    }
}
