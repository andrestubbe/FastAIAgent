@echo off
call mvn compile exec:java -D"exec.mainClass"="demo.chapters.chapter05.PlanningAgentDemo" -q
pause
