package client.ui;

import chess.ChessPosition;
import com.google.gson.Gson;
import result.GameSummary;
import result.ListGamesResult;
import result.UserResult;
import websocket.WsClient;
import websocket.commands.Move;
import websocket.commands.UserGameCommand;

import java.util.Scanner;
public class Client {
    private static String authToken;
    private static String username;
    private static int currentGameID;
    private static boolean isObserver = false;
    private static final Scanner READER = new Scanner(System.in);
    private static final ServerFacade SERVER_FACADE = new ServerFacade(8080);
    private static WsClient wsClient;
    private static final Gson GSON = new Gson();

    public static void main(String[] args) {
        while(true) { // Pre-login UI
            System.out.println("""
                    ♕ Welcome to the 240 Chess Program ♕
                    
                    What would you like to do?
                    1 - Register
                    2 - Login
                    3 - Help
                    4 - Quit""");

            int choice = getInput(4);

            if(choice == 1) { // Register
                register();
            } else if(choice == 2) { // Login
                login();
            } else if(choice == 3) { // Help
                System.out.println("""
                        Register - Allows you to create a new account
                        Login - Prompts you enter your username and password to login
                        Quit - Exits the program
                        """);
            } else if(choice == 4) { break; } // Quit
        }
    }

    private static void postLoginUI() {
        while(true) {
            System.out.printf("""
                    %n♕ Hello %s ♕
                    
                    What would you like to do?
                    1 - Play Game
                    2 - Create Game
                    3 - List Games
                    4 - Observe Game
                    5 - Help
                    6 - Logout%n""", username);

            int choice = getInput(6);

            if(choice == 1) { // Play Game
                joinGame();
            } else if(choice == 2) { // Create Game
                createGame();
            } else if(choice == 3) { // List Games
                listGames();
            } else if(choice == 4) { // Observe Game
                observeGame();
            } else if(choice == 5) { // Help
                System.out.println("""
                        Play Game - Allows you to join a chess game with an open spot
                        Create Game - Creates a new chess game
                        List Games - Lists all games in the database
                        Observe Game - Allows you to observe a game
                        Logout - Logs you out and returns you to the main prompt""");
            } else if(choice == 6) { // Logout
                logout();
                break;
            }
        }
    }

    private static void gamePlayUI() throws InterruptedException {
        while(true) {
            Thread.sleep(500); //So the chessboard prints first
            System.out.println("""
                    What would you like to do?
                    1 - Make Move
                    2 - Highlight Legal Moves
                    3 - Redraw Chess Board
                    4 - Resign
                    5 - Help
                    6 - Leave""");

            int choice = getInput(6);

            if(choice == 1) { // Make Move
                if(!isObserver) {
                    makeMove();
                } else {
                    System.out.println("Observers cannot make moves or resign.");
                }
            } else if(choice == 2) { // Highlight Legal Moves
                showLegalMoves();
            } else if(choice == 3) { // Redraw Chess Board
                redrawBoard();
            } else if(choice == 4) { // Resign
                if(!isObserver) {
                    System.out.println("""
                            Are you sure you want to resign?
                            Enter 1 to resign
                            Ender 2 to cancel""");
                    choice = getInput(2);
                    if (choice == 1) {
                        resign();
                        break;
                    }
                } else {
                    System.out.println("Observers cannot make moves or resign.");
                }
            } else if(choice == 5) { // Help
                System.out.println("""
                        Make Move - Allows you to move a chess piece you control
                        Highlight Legal Moves - Highlights all possible moves that a piece can make
                        Redraw Chess Board - Redraws the chess board at its current state
                        Resign - You willing surrender and loose the game
                        Leave - Leave the game, leaving your spot vacant allowing another to join in your place""");
            } else if(choice == 6) { // Leave
                System.out.println("""
                        Are you sure you want to leave?
                        Enter 1 to leave
                        Ender 2 to cancel""");
                choice = getInput(2);
                if(choice == 1) {
                    leave();
                    break;
                }
            }
        }
    }

    private static int getInput(int numOfChoices) {
        while(true) {
            try {
                int choice = Integer.parseInt(READER.nextLine());
                if(choice < 1 || choice > numOfChoices) {
                    System.out.printf("Invalid input: Please input a number from 1 - %d%n", numOfChoices);
                } else {
                    return choice;
                }
            } catch(Exception e) {
                System.out.printf("Invalid input: Please input a number from 1 - %d%n", numOfChoices);
            }
        }
    }

    private static void register() {
        System.out.println("Enter in the username for the new account:");
        String usernameInput = READER.nextLine();
        System.out.println("Enter in the password for the new account:");
        String password = READER.nextLine();
        System.out.println("Enter in the email for the new account:");
        String email = READER.nextLine();

        try {
            UserResult result = SERVER_FACADE.register(usernameInput, password, email);
            authToken = result.authToken();
            username = result.username();

            System.out.println("Success!");

            postLoginUI();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void login() {
        System.out.println("Enter in the username:");
        String usernameInput = READER.nextLine();
        System.out.println("Enter in the password:");
        String passwordInput = READER.nextLine();

        try {
            UserResult result = SERVER_FACADE.login(usernameInput, passwordInput);
            authToken = result.authToken();
            username = result.username();

            System.out.println("Success!");

            postLoginUI();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            username = null;
        }
    }

    private static void joinGame() {
        System.out.println("Which game would you like to join?");
        int gameID = getGameID();
        if(gameID == -1) { return; }
        System.out.println("Enter in the color you wish to join as:");
        String color = READER.nextLine();

        try {
            currentGameID = gameID;

            //SQL DataBase
            SERVER_FACADE.joinGame(authToken, color, gameID);

            //Websocket
            wsClient = new WsClient(8080); // Initialize websocket connection
            wsClient.send(GSON.toJson(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID)));

            System.out.println("Success!");

            gamePlayUI();
        } catch (Exception e) {
            System.out.println("Error: "+ e.getMessage());
        }
    }

    public static void createGame() {
        System.out.println("Enter in the name of the game: ");
        String gameName = READER.nextLine();

        try {
            SERVER_FACADE.createGame(authToken, gameName);

            System.out.println("Success!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static ListGamesResult listGames() {
        try {
            ListGamesResult result = SERVER_FACADE.listGames(authToken);
            int i = 1;
            for (GameSummary game : result.games()) {
                System.out.printf("%d: Game name: %s | White: %s | Black: %s%n",
                        i++,
                        game.gameName(),
                        game.whiteUsername() == null ? "Open" : game.whiteUsername(),
                        game.blackUsername() == null ? "Open" : game.blackUsername());
            }
            return result;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static void logout() {
        try {
            SERVER_FACADE.logout(authToken);
            username = null;
            authToken = null;

            System.out.println("Success!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void observeGame() {
        System.out.println("What game would you like to observe?");
        int gameID = getGameID();
        if (gameID == -1) { return; }

        try {
            currentGameID = gameID;
            isObserver = true;

            wsClient = new WsClient(8080); // Initialize websocket connection

            wsClient.send(GSON.toJson(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID)));

            System.out.println("Observing game...");

            gamePlayUI(); // reuse same UI (totally fine)
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static int getGameID() {
        ListGamesResult result = listGames();

        if(result == null || result.games().isEmpty()) {
            System.out.println("There are no games available");
            return -1;
        }

        int choice = getInput(result.games().size());

        int counter = 1;
        for(GameSummary game : result.games()) {
            if(counter == choice) { return game.gameID(); }
            counter++;
        }
        return -1;
    }

    private static void makeMove() {
        System.out.println("Enter piece to move (e.g., b2):");
        ChessPosition start = parsePosition();

        System.out.println("Enter destination (e.g., b4):");
        ChessPosition end = parsePosition();

        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, currentGameID, new Move(start, end));

            wsClient.send(GSON.toJson(command));
        } catch (Exception e) {
            System.out.println("Error making move: " + e.getMessage());
        }
    }

    private static void showLegalMoves() {
        System.out.println("Enter in the space of the chess piece you want to see the valid moves of (Example: b4):");
        ChessPosition position = parsePosition();

        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.GET_LEGAL_MOVES, authToken, currentGameID, position);
            wsClient.send(GSON.toJson(command));
        } catch (Exception e) {
            System.out.println("Error resigning: " + e.getMessage());
        }
    }

    private static void redrawBoard() {
        if (WsClient.latestGame == null) {
            System.out.println("No game loaded.");
            return;
        }

        ChessBoardPrinter printer = new ChessBoardPrinter(WsClient.latestGame.getBoard(), WsClient.playerColor);

        System.out.println(printer.buildBoardString());
    }

    private static void resign() {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, currentGameID);
            wsClient.send(GSON.toJson(command));

            System.out.println("You have resigned.");

            wsClient = null;
        } catch (Exception e) {
            System.out.println("Error resigning: " + e.getMessage());
        }
    }

    private static void leave() {
        try {
            isObserver = false;

            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, currentGameID);
            wsClient.send(GSON.toJson(command));

            System.out.println("You have left the game.");

            wsClient = null;
        } catch (Exception e) {
            System.out.println("Error leaving game: " + e.getMessage());
        }
    }

    private static ChessPosition parsePosition() {
        while(true) {
            String pieceInput = READER.nextLine();
            boolean doesMatch = pieceInput.matches("\\b[a-h|A-H][1-8]\\b");

            if(doesMatch) {
                char colChar = Character.toLowerCase(pieceInput.charAt(0));
                int row = Character.getNumericValue(pieceInput.charAt(1));
                int col = colChar - 'a' + 1;

                return new ChessPosition(row, col);
            } else {
                System.out.println("Error: Invalid chess notation. Example: b6");
            }
        }
    }
}
