# ReChor — Swiss Public Transport Route Planner

ReChor is a route planning application for Swiss public transport. It helps users find and compare efficient journeys between two stations.

The project was developed as part of my Computer Science studies at EPFL.

Instead of returning only a single fastest route, ReChor computes several relevant journey options and presents a detailed view of each one, including departure and arrival times, intermediate stops, transfers, and transport segments. The interface allows users to search for routes, compare alternatives, and inspect the details of a selected journey.

---

## Features

### 🧠 Multi-criteria route planning
ReChor uses Pareto-based routing to keep only meaningful journey options. It considers trade-offs such as:
- total travel time
- number of transfers
- dominated routes elimination

This allows the application to display multiple high-quality alternatives rather than a single result.

### 🗺 Detailed journey information
For every suggested journey, ReChor provides:
- the train, bus, or transport line to take
- the departure stop and platform when available
- departure and arrival times
- a clear sequence of all journey segments

### 💻 JavaFX user interface
The application includes a graphical interface with:
- origin and destination search fields
- a list of recommended journeys
- a detailed panel for the selected route
- an optional placeholder for a map view

---

## Tech stack

- **Language:** Java
- **User interface:** JavaFX
- **Algorithms / data structures:** custom public transport routing using Pareto dominance
- **IDE:** IntelliJ IDEA
- **Build:** Standard IntelliJ project without Maven or Gradle. Run `Main.java` directly.

---

## How to run

1. Clone the repository:
   ```bash
   git clone https://github.com/vwalendy/ReChor.git
   cd ReChor
