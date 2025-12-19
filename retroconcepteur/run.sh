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
read -p "Mode (gui / console) [gui]: " MODE
MODE=${MODE:-gui}

if [ "$MODE" = "console" ] || [ "$MODE" = "cui" ]; then
	read -p "Chemin du dossier à analyser (ex: data) : " CHEMIN
	if [ -z "$CHEMIN" ]; then
		echo "Aucun chemin fourni. Annulation."
		exit 1
	fi
	echo "Lancement en mode CUI sur '$CHEMIN'..."
	java -cp class controleur.Controleur console "$CHEMIN"
else
	echo "Lancement en mode GUI..."
	java -cp class controleur.Controleur gui
fi