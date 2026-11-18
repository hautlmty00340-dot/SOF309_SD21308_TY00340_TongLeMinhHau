@echo off
setlocal
cd /d "%~dp0"

echo AU Badminton - SOF309
if not defined DB_URL set "DB_URL=jdbc:sqlserver://localhost;databaseName=AuDB;encrypt=false;trustServerCertificate=true"
if not defined DB_USER set "DB_USER=sa"
if not defined DB_PASSWORD echo [INFO] DB_PASSWORD is not set. Set it in the terminal before launching this file if SQL Server requires a password.

where mvn.cmd >nul 2>&1
if errorlevel 1 (
  where mvn >nul 2>&1
  if errorlevel 1 (
    echo [ERROR] Maven was not found in PATH. Install Maven 3.8+ and reopen the terminal.
    pause
    exit /b 1
  )
)

echo [INFO] Frontend: http://localhost:8080/
echo [INFO] Database: AuDB on SQL Server. Run docs\database\migration_booking_detail.sql in SSMS first.
call mvn -B -ntp spring-boot:run
endlocal
