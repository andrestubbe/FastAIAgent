@echo off
chcp 65001 >nul
call mvn compile exec:java -D"exec.mainClass"="demo.MultiAgentOrchestratorDemo" -q
