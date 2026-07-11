@echo off
set /p GEMINI_API_KEY=<C:\Users\andre\Documents\GEMINI_API_KEY.txt
call mvn compile exec:java -D"exec.mainClass"="demo.CalculationProtocolAgentDemo" -q
pause
