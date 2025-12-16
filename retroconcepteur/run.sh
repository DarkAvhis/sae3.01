#!/bin/bash

set -e

echo "=== Nettoyage des classes précédentes ==="
rm -rf class
mkdir -p class

echo "=== Compilation du projet ==="
javac -d class @compile.list

echo "Compilation réussie"
echo ""
echo "=== Exécution de l'application ==="
java -cp class controleur.Controleur