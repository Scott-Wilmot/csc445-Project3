# 445 - Peer to peer game 
A peer-to-peer approach to a non-copyrighted multiplayer game

## Folder structure

```
csc445-Project3/
├── README.md                
├── pom.xml               
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/mycompany/app/
│   │           ├── App.java               # Application entry point
│   │           ├── controller/            # Controllers for game logic and scene flow
│   │           ├── game/                  # Game engine, state management, and turn handling
│   │           ├── model/                 # Core game entities (Card, Player, GameState)
│   │           ├── net/                   # Server and client networking logic, message passing
│   │           ├── ui/                    # JavaFX scenes, components, and layout
│   │           └── utils/                 # Utility classes (e.g., Logger, Constants)
│   └── test/
│       └── java/com/mycompany/app/
│           └── AppTest.java             
└── target/                               
    ├── classes/                        
    └── generated-sources/            

```