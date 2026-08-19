@echo off
setlocal
cd /d "%~dp0"
call mvn compile exec:java -Dexec.mainClass="demo.CodingAgentLoopDemo"
