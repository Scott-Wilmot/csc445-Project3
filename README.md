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

# Setup and Installation
1. Clone the repository
2. Create a `cardImages` folder under `src/main/resources`
3. Copy card images( download from [link](https://drive.google.com/file/d/1wNBbTLSjaluWTWW3rw3SRt3pTWFQZWLl/view?usp=sharing) ) to the `cardImages` folder
4. Build the project:
   ```bash
   ./mvnw clean install
   ```
5. Running the project:
    ```bash
   ./mvnw javafx:run
   ```

# Game Rules
### 📝 Basic Rules
- 🃏 Each player starts with 7 cards.

- 🔄 On your turn, play a card that matches the color or number of the top card on the discard pile.

- 🚫 If you can't play a valid card, draw one from the deck.

- 🎯 The goal is to be the first to get rid of all your cards.

### 💥 Special Cards
- ⏭️ Skip – The next player loses their turn.

- 🔁 Reverse – Reverses the turn order.

- ➕2 Draw Two – The next player draws 2 cards and loses their turn.

- 🌈 Wild – Can be played on any color; choose a new color.

- ➕4 Wild Draw Four – The next player draws 4 cards, loses their turn, and you choose the new color. 



MIT License

Copyright (c) [2025] (Phone Pyae Sone Phyo, Crislenny Uceta, Saurav Lamichhane, Scott Wilmot) 

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.