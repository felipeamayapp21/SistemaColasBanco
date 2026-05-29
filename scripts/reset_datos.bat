@echo off
chcp 65001 >nul
cd /d "%~dp0.."
title BancoApp - Reset datos (100 clientes nuevos)

set "PGPASSWORD=123456789"
set "PGUSER=postgres"
set "PGHOST=localhost"

set "PSQL="
if exist "C:\Program Files\PostgreSQL\17\bin\psql.exe" set "PSQL=C:\Program Files\PostgreSQL\17\bin\psql.exe"
if exist "C:\Program Files\PostgreSQL\16\bin\psql.exe" set "PSQL=C:\Program Files\PostgreSQL\16\bin\psql.exe"
if exist "C:\Program Files\PostgreSQL\15\bin\psql.exe" set "PSQL=C:\Program Files\PostgreSQL\15\bin\psql.exe"

if "%PSQL%"=="" (
    echo [ERROR] No se encontro psql.exe. Instala PostgreSQL.
    pause
    exit /b 1
)

echo.
echo  Regenerando seed.sql con 100 clientes...
where py >nul 2>&1 && py -3 database\generar_seed.py
if errorlevel 1 (
    "C:\Users\felip\AppData\Local\Programs\Python\Python311\python.exe" database\generar_seed.py
)

echo.
echo  Borrando datos anteriores e insertando 100 clientes nuevos...
"%PSQL%" -h %PGHOST% -U %PGUSER% -d banco_db -f "database\seed.sql"

if errorlevel 1 (
    echo [ERROR] Fallo al cargar seed.sql
    pause
    exit /b 1
)

echo.
echo  Listo: 4 usuarios + 100 clientes + historial de atendidos.
echo  Login: admin / admin123
echo.
pause
