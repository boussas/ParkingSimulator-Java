package com.example.ParkingSimulator;
import java.util.Random;
// la classe Voiture
public class Voiture extends Thread {
	private final String nom;
	private final Parking parking;
	private int place;

	public Voiture(String nom, Parking parking) {
		this.nom = nom;
		this.parking = parking;
	}
	public String getNom() {
		return this.nom;
	}

	public void setPlace(int place) {
		this.place = place;
	}

	public int getPlace() {
		return this.place;
	}

	Random random = new Random();

	@Override
	public void run() {
		try {
			// Essayer de stationner la voiture
			parking.stationner(this);

			// Faire dormir le thread pour simuler le temps pendant lequel la voiture reste stationnée
			Thread.sleep(random.nextInt(2000) + 10000);

			// Essayer de sortir la voiture du parking
			parking.sortir(this);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
