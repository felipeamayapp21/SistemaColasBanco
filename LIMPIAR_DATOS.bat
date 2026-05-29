@echo off
REM Lanzador para limpiar la base de datos de manera segura sin borrar la BD.
cd /d "%~dp0"
call "%~dp0scripts\limpiar_datos.bat"
