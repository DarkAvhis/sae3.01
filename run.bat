@echo off

REM Compilation
echo === Compilation du projet ===
javac -d bin @compile.list

if %ERRORLEVEL% equ 0 (
    echo Compilation reussie
    echo.
    echo === Execution de l'application ===
    java -cp bin vue.FenetrePrincipale
) else (
    echo Erreur lors de la compilation
    exit /b 1
)