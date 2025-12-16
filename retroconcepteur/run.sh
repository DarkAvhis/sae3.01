#!/bin/bash

# Compilation
echo "=== Compilation du projet ==="
cd src
javac -d class @compile.list

if [ $? -eq 0 ]; then
    echo "Compilation réussie"
    echo ""
    echo "=== Exécution de l'application ==="
    java -cp class controleur.Controleur
else
    echo "Erreur lors de la compilation"
    exit 1
fi