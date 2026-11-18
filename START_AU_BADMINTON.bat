@echo off
setlocal EnableExtensions
cd /d "%~dp0"

echo AU Badminton - SOF309
if not defined DB_URL set "DB_URL=jdbc:sqlserver://localhost;databaseName=AuDB;encrypt=false;trustServerCertificate=true"
if not defined DB_USER set "DB_USER=sa"
if not defined DB_PASSWORD echo [INFO] DB_PASSWORD is not set. Set it before launching if SQL Server requires a password.

set "MAVEN_CMD="
for /f "delims=" %%M in ('where mvn.cmd 2^>nul') do if not defined MAVEN_CMD set "MAVEN_CMD=%%M"
if not defined MAVEN_CMD for /f "delims=" %%M in ('where mvn 2^>nul') do if not defined MAVEN_CMD set "MAVEN_CMD=%%M"
if not defined MAVEN_CMD (
  for /d %%D in ("C:\Tools\apache-maven-*") do if exist "%%~fD\bin\mvn.cmd" set "MAVEN_CMD=%%~fD\bin\mvn.cmd"
)
if not defined MAVEN_CMD (
  echo [ERROR] Maven was not found. Install Maven 3.8+ or add its bin folder to PATH.
  pause
  exit /b 1
)

set "MAVEN_OPTS=-Dfile.encoding=UTF-8"
echo [INFO] Maven: %MAVEN_CMD%
echo [INFO] Frontend: http://localhost:8080/
echo [INFO] Database: AuDB on SQL Server. Run docs\database\migration_booking_detail.sql in SSMS first.
call "%MAVEN_CMD%" -B -ntp spring-boot:run
set "APP_EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %APP_EXIT_CODE%
