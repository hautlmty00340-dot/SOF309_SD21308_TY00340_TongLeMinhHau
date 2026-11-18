package com.aubadminton;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SchemaUpdates {
    @Bean
    ApplicationRunner updateSchema(JdbcTemplate db) {
        return args -> {
            db.execute("IF COL_LENGTH('dbo.users','email') IS NULL ALTER TABLE dbo.users ADD email NVARCHAR(150) NOT NULL CONSTRAINT DF_users_email DEFAULT ''");
            db.execute("IF COL_LENGTH('dbo.users','address') IS NULL ALTER TABLE dbo.users ADD address NVARCHAR(250) NOT NULL CONSTRAINT DF_users_address DEFAULT ''");
            db.execute("IF COL_LENGTH('dbo.users','avatar_style') IS NULL ALTER TABLE dbo.users ADD avatar_style VARCHAR(24) NOT NULL CONSTRAINT DF_users_avatar DEFAULT 'forest'");
            db.execute("IF COL_LENGTH('dbo.users','avatar_image') IS NULL ALTER TABLE dbo.users ADD avatar_image NVARCHAR(500) NOT NULL CONSTRAINT DF_users_avatar_image DEFAULT ''");
            db.execute("IF OBJECT_ID('dbo.app_settings','U') IS NULL CREATE TABLE dbo.app_settings (setting_key VARCHAR(60) PRIMARY KEY, setting_value NVARCHAR(500) NOT NULL)");
        };
    }
}
