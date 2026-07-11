@echo off
echo [FastAIAgent] Compiling and running Chapter 5 Planning Agent Demo...
call mvn compile exec:java -D"exec.mainClass"="demo.chapters.chapter05.PlanningAgentDemo"
pause
