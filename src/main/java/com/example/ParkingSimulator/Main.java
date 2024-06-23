package com.example.ParkingSimulator;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class Main extends Application {
	private CustomRectangle[][] matrix;
	private int syncStrategy; // Variable pour stocker la stratégie de synchronisation choisie
	private int numCars; // Nombre de voitures
	private int nbrPlaces; // Nombre de places

	@Override
	public void start(Stage primaryStage) {
		// Obtenir la stratégie de synchronisation de l'utilisateur
		syncStrategy = getSyncStrategyFromUser();
		// Obtenir le nombre de voitures de l'utilisateur
		numCars = getNumFromUser("Nombre de voitures", "Entrez le nombre de voitures:");
		// Obtenir le nombre de places de parking de l'utilisateur
		nbrPlaces = getNumFromUser("Nombre de places de parking", "Entrez le nombre de places de parking:");

		// Calculer le nombre de colonnes et de lignes
		int numColumns = (int) Math.ceil(Math.sqrt(nbrPlaces));
		int numRows = (int) Math.ceil((double) nbrPlaces / numColumns);

		// Initialiser la grille
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10));
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setAlignment(Pos.CENTER); // Centrer la grille
		matrix = new CustomRectangle[numColumns][numRows];

		// Initialiser et ajouter des rectangles personnalisés à la grille
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numColumns; col++) {
				matrix[col][row] = new CustomRectangle();
				grid.add(matrix[col][row], col, row);
				if (row * numColumns + col < nbrPlaces) {
					setRectangleColor(col, row, Color.GREEN); // Définit la couleur verte pour les places disponibles
				} else {
					setRectangleColor(col, row, Color.GREY); // Définit la couleur grise pour les places non disponibles
				}
			}
		}

		// Créer un bouton pour afficher les statistiques de temps d'attente
		Button statsButton = new Button("Afficher la moyenne d'attente");
		statsButton.setStyle(
				"-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-size: 14px; -fx-font-family: 'Arial'; -fx-border-radius: 5px; -fx-background-radius: 5px;"
		);
		statsButton.setOnAction(e -> {
			String stats = Analyze("../app.log"); // Analyser le fichier de log pour obtenir les statistiques
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Statistiques de temps d'attente");
			alert.setHeaderText(null);
			alert.setContentText(stats);
			alert.showAndWait();
		});

		// Créer un conteneur vertical (VBox) pour la grille et le bouton
		VBox vbox = new VBox(10);
		vbox.setPadding(new Insets(15));
		vbox.setAlignment(Pos.CENTER); // Centrer le contenu du VBox
		vbox.getChildren().addAll(grid, statsButton);

		// Créer et afficher la scène
		Scene scene = new Scene(vbox, 400, 400);
		primaryStage.setTitle("Parking system");
		primaryStage.setScene(scene);
		primaryStage.show();

		// Initialiser la stratégie par défaut et le parking
		IStrategy defaultStrategy = new DefaultStrategy();
		Parking p = new Parking(nbrPlaces, defaultStrategy, this, syncStrategy);
		Random random = new Random();
		for (int t = 0; t < numCars; t++) {
			final int finalT = t;
			PauseTransition pause = new PauseTransition(Duration.millis(random.nextInt(3000) + 5000));
			pause.setOnFinished(event -> {
				Voiture v = new Voiture("Vec " + finalT, p); // Créer une nouvelle voiture
				v.start(); // Démarrer la voiture
			});
			pause.play(); // Démarrer la pause
		}
	}

	// Méthode pour définir la couleur d'un rectangle spécifique
	public void setRectangleColor(int col, int row, Color color) {
		if (col >= 0 && col < matrix.length && row >= 0 && row < matrix[col].length) {
			matrix[col][row].setColor(color);
		}
	}

	// Méthode pour définir le texte d'un rectangle spécifique
	public void setRectangleText(int col, int row, String text) {
		if (col >= 0 && col < matrix.length && row >= 0 && row < matrix[col].length) {
			matrix[col][row].setText(text);
		}
	}

	// Méthode pour obtenir la stratégie de synchronisation de l'utilisateur
	private int getSyncStrategyFromUser() {
		TextInputDialog dialog = new TextInputDialog("1");
		dialog.setTitle("Choix de la stratégie de synchronisation");
		dialog.setHeaderText("Sélectionnez la stratégie de synchronisation");
		dialog.setContentText("Entrez 1 pour Sémaphore ou 2 pour Mutex:");

		Optional<String> result = dialog.showAndWait();
		return result.map(Integer::parseInt).orElse(1); // Par défaut, utiliser le sémaphore
	}

	// Méthode pour obtenir un nombre de l'utilisateur
	private int getNumFromUser(String title, String content) {
		TextInputDialog dialog = new TextInputDialog("10");
		dialog.setTitle(title);
		dialog.setHeaderText(null);
		dialog.setContentText(content);

		Optional<String> result = dialog.showAndWait();
		return result.map(Integer::parseInt).orElse(10); // Par défaut, utiliser 10
	}

	// Méthode pour analyser le fichier de log et obtenir des infos sur le temps d'attente
	public static String Analyze(String filePath) {
		List<Double> tempsAttenteList = new ArrayList<>();
		double totalAttente = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("Temps d'attente pour la voiture")) {
					int startIndex = line.lastIndexOf(":") + 2;
					int endIndex = line.lastIndexOf(" millisecondes");
					String attenteStr = line.substring(startIndex, endIndex);
					// Enlever tous les caractères non numériques
					attenteStr = attenteStr.replaceAll("[^\\d.]", "");
					double tempsAttente = Double.parseDouble(attenteStr);
					tempsAttenteList.add(tempsAttente);
					totalAttente += tempsAttente;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		StringBuilder result = new StringBuilder();
		if (!tempsAttenteList.isEmpty()) {
			result.append("Temps d'attente de chaque voiture :\n");
			for (int i = 0; i < tempsAttenteList.size(); i++) {
				result.append("Voiture ").append(i + 1).append(": ").append(tempsAttenteList.get(i)).append(" millisecondes\n");
			}

			double moyenneAttente = totalAttente / tempsAttenteList.size();
			result.append("La moyenne d'attente des voitures est de : ").append(moyenneAttente / 1000).append(" Secondes");
		} else {
			result.append("Aucune information sur l'attente des voitures trouvée dans le fichier journal.");
		}
		return result.toString();
	}

	public static void main(String[] args) {
		launch(args);
	}
}