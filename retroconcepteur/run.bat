@echo off
echo === Compilation du projet ===

REM Compilation
javac -d class @compile.list
IF ERRORLEVEL 1 (
    echo Erreur de compilation.
    pause
    exit /b 1
)

echo Compilation réussie
echo.
echo === Execution de l'application ===

REM Choix du mode
set MODE=gui
set /p MODE="Mode (gui / console) [gui]: "

IF "%MODE%"=="" set MODE=gui

IF /I "%MODE%"=="console" GOTO CONSOLE
IF /I "%MODE%"=="cui" GOTO CONSOLE

REM Mode GUI
echo Lancement en mode GUI...
java -cp class controleur.Controleur gui
GOTO END

:CONSOLE
set CHEMIN=
set /p CHEMIN="Chemin du dossier a analyser (ex: data) : "

IF "%CHEMIN%"=="" (
    echo Aucun chemin fourni. Annulation.
    pause
    exit /b 1
)

echo Lancement en mode CUI sur "%CHEMIN%"...
java -cp class controleur.Controleur console "%CHEMIN%"

:END
pause
