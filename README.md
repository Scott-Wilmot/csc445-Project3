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
│   │           ├── communication/
│   │           ├── model
│   │           ├── ui
│   └── test/
│       └── java/com/mycompany/app/
│           └── AppTest.java             
└── target/                               
    ├── classes/                        
    └── generated-sources/            

```

## Running User Interface for JavaFX
Before running, please create a folder named "cardImages" under "src/main/resources"

Copy all images from deck-renamed and put into the "cardImages" folder
```java
./mvnw javafx:run
```