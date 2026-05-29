@echo off
chcp 65001 >nul
cd /d "%~dp0.."

set "PGPASSWORD=123456789"
set "PGUSER=postgres"
set "PGHOST=localhost"

set "PSQL="
if exist "C:\Program Files\PostgreSQL\17\bin\psql.exe" set "PSQL=C:\Program Files\PostgreSQL\17\bin\psql.exe"
if exist "C:\Program Files\PostgreSQL\16\bin\psql.exe" set "PSQL=C:\Program Files\PostgreSQL\16\bin\psql.exe"
if exist "C:\Program Files\PostgreSQL\15\bin\psql.exe" set "PSQL=C:\Program Files\PostgreSQL\15\bin\psql.exe"

if "%PSQL%"=="" (
    echo [ERROR] No se encontro psql.exe. Instala PostgreSQL.
    exit /b 1
)

echo [BD] Usando: %PSQL%
echo [BD] Creando base de datos banco_db si no existe...
"%PSQL%" -h %PGHOST% -U %PGUSER% -tc "SELECT 1 FROM pg_database WHERE datname='banco_db'" | findstr /C:"1" >nul
if errorlevel 1 (
    "%PSQL%" -h %PGHOST% -U %PGUSER% -c "CREATE DATABASE banco_db;"
    if errorlevel 1 (
        echo [ERROR] No se pudo crear banco_db.
        exit /b 1
    )
)

echo [BD] Ejecutando schema.sql...
"%PSQL%" -h %PGHOST% -U %PGUSER% -d banco_db -f "database\schema.sql"

echo [BD] Ejecutando migration_seguridad.sql (recuperacion de contrasena y documento)...
"%PSQL%" -h %PGHOST% -U %PGUSER% -d banco_db -f "database\migration_seguridad.sql"

echo [BD] Ejecutando seed.sql...
"%PSQL%" -h %PGHOST% -U %PGUSER% -d banco_db -f "database\seed.sql"

echo [BD] Base de datos lista.
exit /b 0
