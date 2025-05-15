# 445 - Peer to peer game 
A peer-to-peer approach to a non-copyrighted multiplayer game

## Folder structure

```
├── README.md               # Project overview and usage instructions
├── mvnw                   # Maven wrapper script (Linux/macOS)
├── mvnw.cmd               # Maven wrapper script (Windows)
├── pom.xml                # Maven project configuration
├── src
│   ├── main
│      ├── java
│      │   └── com.mycompany.app
│      │       ├── communication       # Networking and encryption (Client, Host, etc.)
│      │       ├── model              # Game data models (Card, Player, etc.)
│      │       ├── testingenvironment # Test clients and mock host
│      │       └── ui
│      │           ├── temp           # Temporary UI files
│      │           ├── uiController   # JavaFX UI controllers
│      │           └── utils          # Helper utilities
│      └── resources
│          ├── cardImages             # Image assets for cards
└── target
    ├── Client.jar                     # Compiled client application
    ├── Server.jar                     # Compiled server application
    └── classes                        # Compiled class and resource files
```

## Running User Interface for JavaFX
Before running, please create a folder named "cardImages" under "src/main/resources"

Copy all images from deck-renamed and put into the "cardImages" folder
```java
./mvnw javafx:run
```

# Overview
This project implements a multiplayer card game with the following key features:
- Peer-to-peer networking using UDP
- JavaFX-based user interface
- Raft consensus protocol for leader election
- Real-time game state synchronization
- Support for up to 4 players

# Components

### User Interface
- JavaFX-based UI with 
    - MainView
    - RoomView
- Each View is associated with its respective controller to communicate with other components
  - `RoomViewController.java`
  - `MainController.java`
- Includes Card Visualization, Management, and Interactive operations

### GameState
- `GameState.java`: Manages game state class
- `Card.java`: Card representation class
- `Player.java`: Player management class
- `Shape.java` & `Value.java`: Card attributes class

### Communication Layer
- `Client.java`: Handles client-side communication
- `Host.java`: Manages server-side communication
- `Packet.java`: Defines network packet structure
- `Encryption.java`: Encrypts packets back and forth between host and client

### Raft Implementation
- Leader election
- Heartbeat mechanism
- State synchronization
- Term management


