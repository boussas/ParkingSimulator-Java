# Parking Simulator

### A Java-based Parking Simulator application using JavaFX for the graphical user interface and a customizable parking strategy. This project simulates a parking lot where cars attempt to park using different synchronization strategies (Semaphore or Mutex).

## Features:

* Customizable Parking Spot Display: Uses CustomRectangle class to represent parking spots with customizable colors and labels.
* Synchronization Strategies: Implements different synchronization strategies (Semaphore and Mutex) through the IStrategy interface and DefaultStrategy class.
* Interactive GUI: JavaFX-based GUI that prompts the user for the number of cars and parking spots, and displays the parking status in a grid layout.
* Logging: Logs parking attempts and waiting times to a file.
* Simulation and Animation: Simulates car parking using pauses and animations.
* Thread Management: Program oriented to thread management, where a critical section (parking) is managed using Mutex for synchronization.

## Project Structure:

* CustomRectangle.java: A custom JavaFX component representing a parking spot.
* DefaultStrategy.java: Implements the default parking strategy to find the next available spot.
* IStrategy.java: Interface for different parking strategies.
* Main.java: Main application class that sets up the JavaFX interface and starts the simulation.
* Parking.java: Manages parking spots and synchronization strategies.
* Voiture.java: Represents a car trying to park in the parking lot.


## Thread Management and Critical Section
The application is designed to manage multiple threads representing cars trying to park simultaneously. The critical section (parking operation) is protected using Mutex or Semaphore to ensure thread safety and prevent race conditions. This will avoid conflicts and ensure consistent state updates.

## Graphical User Interface (GUI):
* Grid Layout: The parking spots are displayed in a grid layout using JavaFX's GridPane. Each spot is represented by a CustomRectangle which can change color based on availability (green for available, grey for unavailable).
* User Prompts: On startup, the application prompts the user to input the number of cars and parking spots, and to choose a synchronization strategy.
* Statistics Button: A button labeled "Afficher la moyenne d'attente" allows the user to view the average waiting time for cars, calculated from the log file.

## How to Run :
You can directly use your IDE or:

Clone the repository:
```
git clone https://github.com/your-username/parking-simulator.git
cd parking-simulator
```
Build and run the project using Make: 
```
make all
make run
```

## Usage:
Upon running the application, you will be prompted to:

  1. Select a synchronization strategy (Semaphore or Mutex).
  2. Enter the number of cars.
  3. Enter the number of parking spots.

The application will then display a grid representing the parking lot, with cars attempting to park at random intervals.
You can view the average waiting time for cars by clicking the "Afficher la moyenne d'attente" button.

## Logging:
The application logs parking attempts and waiting times to app.log.
* Log Analysis:
The Analyze method reads the log file and calculates the average waiting time for cars. It displays detailed information about each car's waiting time in a dialog box.
