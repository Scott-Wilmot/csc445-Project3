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

