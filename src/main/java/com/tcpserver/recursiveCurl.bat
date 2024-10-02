@echo off
:loop
curl -X POST http://localhost:8082 -d "Hello from curl!"
timeout /t 1 >nul
goto loop
