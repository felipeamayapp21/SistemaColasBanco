@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul
cd /d "%~dp0.."
set "ROOT=%CD%"
set "APP_URL=http://localhost:5173"
title BancoApp - %APP_URL%

echo.
echo  ============================================================
echo       BANCO APP - Sistema de Gestion de Clientes
echo  ============================================================
echo.

set "JAVA_HOME="
if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot" set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot"
if "!JAVA_HOME!"=="" if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot" set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
if "!JAVA_HOME!"=="" if exist "C:\Program Files\Java\jdk-21" set "JAVA_HOME=C:\Program Files\Java\jdk-21"

if "!JAVA_HOME!"=="" (
    echo [ERROR] Instala Java 21 JDK.
    pause
    exit /b 1
)

if exist "C:\Program Files\PostgreSQL\17\bin" set "PATH=%PATH%;C:\Program Files\PostgreSQL\17\bin"
if exist "C:\Program Files\PostgreSQL\16\bin" set "PATH=%PATH%;C:\Program Files\PostgreSQL\16\bin"
if exist "C:\Program Files\PostgreSQL\15\bin" set "PATH=%PATH%;C:\Program Files\PostgreSQL\15\bin"

echo [1/5] Base de datos PostgreSQL...
call "%ROOT%\scripts\inicializar_bd.bat"
if not "!ERRORLEVEL!"=="0" (
    echo [ERROR] PostgreSQL - revisa clave 123456789 en application.properties
    pause
    exit /b 1
)

echo [2/5] Dependencias npm...
if not exist "%ROOT%\frontend\node_modules" (
    cd /d "%ROOT%\frontend"
    call npm install
    cd /d "%ROOT%"
)

echo [3/5] Compilando backend...
cd /d "%ROOT%\backend"
call "%ROOT%\backend\mvnw.cmd" package -DskipTests -q
if not "!ERRORLEVEL!"=="0" (
    echo [ERROR] Fallo la compilacion del backend.
    pause
    exit /b 1
)

echo [4/5] Backend Java (ventana aparte, puerto 8080)...
start "Backend Java" cmd /k ""!JAVA_HOME!\bin\java" -jar "%ROOT%\backend\target\backend-0.0.1-SNAPSHOT.jar""

echo [5/5] Frontend Metro UI (esta ventana, puerto 5173)...
echo.
echo    PAGINA:  %APP_URL%
echo    LOGIN:   admin / admin123
echo.
start /min "Abrir navegador" cmd /c "call "%ROOT%\scripts\abrir_pagina.bat""

cd /d "%ROOT%\frontend"
npm run dev

pause >nul
endlocal
