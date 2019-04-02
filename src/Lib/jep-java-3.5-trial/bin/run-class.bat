@echo off
echo ------------------------------------------------------------------------------
echo This batch file may not run correctly if executed from a path containing space characters.
echo. 
echo Lauching %1 ...
if not "%OS%"=="Windows_NT" goto win9xStart

:winNTStart
@setlocal
rem %~dp0 is name of current script under NT
set JEP_HOME=%~dp0
set JEP_HOME=%JEP_HOME%\..
echo JEP_HOME = %JEP_HOME%
set CLASSPATH=%JEP_HOME%\build\
echo CLASSPATH=%CLASSPATH%
echo ------------------------------------------------------------------------------
echo. 
echo.
call java.exe -Xss1m %1
@endlocal
goto mainEnd


:win9xStart
echo No Windows 9x batch support yet...
goto mainEnd


:mainEnd
pause
