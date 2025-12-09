#!/bin/bash

# Compilation
echo "=== Compilation du projet ==="
javac -d bin @compile.list

if [ $? -eq 0 ]; then
    echo "Compilation réussie"
    echo ""
    echo "=== Exécution de l'application ==="
    java -cp bin vue.FenetrePrincipale
else
    echo "Erreur lors de la compilation"
    exit 1
fi