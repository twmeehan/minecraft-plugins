@echo off
setlocal EnableDelayedExpansion

REM Create main target directory if it doesn't exist
if not exist target (
    mkdir target
)

REM Define plugins
set plugins[0]=DynamicSpells
set plugins[1]=health-display
set plugins[2]=keep-inventory-alternative
set plugins[3]=dungeons
set plugins[4]=berry-economy

REM Display menu
echo Available plugins:
for /L %%i in (0,1,4) do (
    set /A displayNum=%%i+1
    echo !displayNum!. !plugins[%%i]!
)
echo 0. Build all plugins
echo q. Quit

REM Get user choice
set /p choice=Enter the number of the plugin to build (or 0 for all, q to quit): 

REM Process choice
if /I "%choice%"=="q" (
    echo Exiting...
    exit /b
)

if "%choice%"=="0" (
    echo Building all plugins...
    for /L %%i in (0,1,4) do (
        call :build_plugin "!plugins[%%i]!"
    )
    goto end
)

REM Validate choice is a number between 1 and 5
set /A choiceIndex=%choice%-1
if %choiceIndex% GEQ 0 if %choiceIndex% LEQ 4 (
    call :build_plugin "!plugins[%choiceIndex%]!"
) else (
    echo Invalid choice. Please run the script again.
    exit /b 1
)

goto end

:build_plugin
set plugin_name=%~1
echo Building %plugin_name%...
pushd "%plugin_name%"
call mvn clean package

REM Copy JAR file to main target directory (excluding original-*.jar)
for %%f in (target\*.jar) do (
    set jarName=%%~nxf
    echo Checking file: !jarName!
    echo !jarName! | findstr /i /r "^original-.*\.jar$" >nul
    if errorlevel 1 (
        copy "%%f" "..\target\" >nul
    )
)
popd
goto :eof

:end
echo Build complete! JAR files are in the main target directory.
