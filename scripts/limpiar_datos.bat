@echo off
chcp 65001 >nul
cd /d "%~dp0.."
title BancoApp - Limpiar Datos

set "PGPASSWORD=123456789"
set "PGUSER=postgres"
set "PGHOST=localhost"

set "PSQL="
if exist "C:\Program Files\PostgreSQL\17\bin\psql.exe" set "PSQL=C:\Program Files\PostgreSQL\17\bin\psql.exe"
if exist "C:\Program Files\PostgreSQL\16\bin\psql.exe" set "PSQL=C:\Program Files\PostgreSQL\16\bin\psql.exe"
if exist "C:\Program Files\PostgreSQL\15\bin\psql.exe" set "PSQL=C:\Program Files\PostgreSQL\15\bin\psql.exe"

if "%PSQL%"=="" (
    echo [ERROR] No se encontró psql.exe en las rutas por defecto.
    echo Asegúrate de tener PostgreSQL instalado correctamente.
    pause
    exit /b 1
)

echo.
echo  ============================================================
  echo  VACIANDO BASE DE DATOS BANCOAPP
  echo  ============================================================
echo.
echo  Se eliminarán todos los clientes, historial de turnos y cajeros.
echo  Se mantendrá únicamente el usuario administrador inicial para iniciar sesión.
echo.
set /p CONFIRM="¿Estás seguro de que deseas limpiar los datos? (S/N): "

if /i "%CONFIRM%" neq "S" (
    echo.
    echo  Operación cancelada.
    echo.
    pause
    exit /b 0
)

echo.
echo  Limpiando base de datos banco_db...
"%PSQL%" -h %PGHOST% -U %PGUSER% -d banco_db -f "database\limpiar.sql"

if errorlevel 1 (
    echo [ERROR] Falló la ejecución del script de limpieza.
    pause
    exit /b 1
)

echo.
echo  ============================================================
echo  ¡Base de datos limpiada con éxito!
echo  ============================================================
echo.
echo  Los datos de atención e historial han sido vaciados.
echo  El sistema quedó limpio y listo para usarse desde cero.
echo.
echo  Usuario administrador inicial reactivado:
echo    - Usuario: admin
echo    - Contraseña: admin123
echo.
pause
