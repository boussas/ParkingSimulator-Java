package com.example.ParkingSimulator;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.paint.Color;

public class Parking {
	private static final Logger LOGGER = Logger.getLogger(Parking.class.getName());
	private final Semaphore placesSemaphore;
	private final Lock placesLock = new ReentrantLock();
	private final List<Long> tempPlace = new ArrayList<>();
	private final List<Boolean> placeOccupe = new ArrayList<>();
	private final IStrategy strategy;
	private final int nbrPlaces;
	private final Main gui;
	private final int syncStrategy; // 1 pour Sémaphore, 2 pour Mutex

	public Parking(int nbrPlaces, IStrategy strategy, Main main, int syncStrategy) {
		// Configuration du fichier de journalisation
		try {
			FileHandler fileHandler = new FileHandler("../app.log", false);
			fileHandler.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fileHandler);
			LOGGER.info("Fichier journalisation configuré avec succès");
		} catch (Exception e) {
			LOGGER.severe("Échec de la configuration du fichier de journalisation : " + e.getMessage());
			System.exit(1);
		}
		// Initialisation des sémaphores et des mutex
		this.placesSemaphore = new Semaphore(nbrPlaces, true);
		this.syncStrategy = syncStrategy;
		// Initialiser la liste contenant le temps nécessaire pour sortir de chaque place et la liste des places occupées
		for (int i = 0; i < nbrPlaces; i++) {
			tempPlace.add((i % 5 + 1) * 1000L); // La liste des valeurs possibles est [1000, 2000, 3000, 4000, 5000]
			placeOccupe.add(false);
		}
		// Initialiser la stratégie
		this.strategy = strategy;
		// Initialiser le nombre de places
		this.nbrPlaces = nbrPlaces;
		// Initialiser l'interface graphique
		this.gui = main;
	}

	public void stationner(Voiture voiture) throws Exception {
		long startAttemptTime = System.currentTimeMillis(); // Enregistrer le début de l'essai de stationnement
		LOGGER.info("La voiture " + voiture.getNom() + " essaie de stationner");

		// Acquérir une place de parking selon la stratégie de synchronisation
		if (syncStrategy == 1) {
			placesSemaphore.acquire();
		} else if (syncStrategy == 2) {
			placesLock.lock();
		}

		try {
			int place = strategy.trouverPlace(tempPlace, placeOccupe, nbrPlaces);
			placeOccupe.set(place, true); // Marquer la place comme occupée
			int numColumns = (int) Math.ceil(Math.sqrt(nbrPlaces));
			int col = place % numColumns;
			int row = place / numColumns;
			gui.setRectangleColor(col, row, Color.RED); // Mettre à jour la couleur de la place dans l'interface graphique
			gui.setRectangleText(col, row, voiture.getNom()); // Mettre à jour le texte de la place dans l'interface graphique
			voiture.setPlace(place); // Assigner la place à la voiture
			LOGGER.info("La voiture " + voiture.getNom() + " a stationné !");
		} finally {
			// Libérer le verrou si la stratégie de synchronisation est le mutex
			if (syncStrategy == 2) {
				placesLock.unlock();
			}
		}

		long endAttemptTime = System.currentTimeMillis(); // Enregistrer la fin du stationnement (réussi ou non)
		long parkingWaitTime = endAttemptTime - startAttemptTime; // Calculer le temps d'attente
		LOGGER.info("Temps d'attente pour la voiture " + voiture.getNom() + " : " + parkingWaitTime + " millisecondes");
	}

	public void sortir(Voiture voiture) {
		LOGGER.info("La voiture " + voiture.getNom() + " essaie de sortir");

		// Acquérir le verrou si la stratégie de synchronisation est le mutex
		if (syncStrategy == 2) {
			placesLock.lock();
		}

		try {
			Thread.sleep(tempPlace.get(voiture.getPlace())); // Simuler le temps nécessaire pour sortir de la place
			placeOccupe.set(voiture.getPlace(), false); // Marquer la place comme libre
			int numColumns = (int) Math.ceil(Math.sqrt(nbrPlaces));
			int col = voiture.getPlace() % numColumns;
			int row = voiture.getPlace() / numColumns;
			gui.setRectangleColor(col, row, Color.GREEN); // Mettre à jour la couleur de la place dans l'interface graphique
			gui.setRectangleText(col, row, ""); // Effacer le texte de la place dans l'interface graphique
			LOGGER.info("La voiture " + voiture.getNom() + " a sorti !");
		} catch (Exception e) {
			LOGGER.warning("Exception " + e.getMessage());
		} finally {
			// Libérer la place de parking selon la stratégie de synchronisation
			if (syncStrategy == 1) {
				placesSemaphore.release();
			} else if (syncStrategy == 2) {
				placesLock.unlock();
			}
		}
	}
}
