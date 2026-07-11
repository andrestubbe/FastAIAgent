@echo off
echo [FastAIAgent] Compiling and running cognitive loop demo...
call mvn compile exec:java -D"exec.mainClass"="demo.AgentDemo"
pause
