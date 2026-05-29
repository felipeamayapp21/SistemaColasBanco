@echo off
set "APP_URL=http://localhost:5173"
set /a N=0

:esperar
set /a N+=1
if %N% gtr 120 goto abrir

powershell -NoProfile -Command "try { (Invoke-WebRequest -Uri '%APP_URL%' -UseBasicParsing -TimeoutSec 2).StatusCode; exit 0 } catch { exit 1 }" >nul 2>&1
if not errorlevel 1 goto abrir

timeout /t 2 /nobreak >nul
goto esperar

:abrir
start "" "%APP_URL%"
exit /b 0
