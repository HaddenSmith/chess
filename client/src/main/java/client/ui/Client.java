package client.ui;

import chess.ChessBoard;
import result.GameSummary;
import result.ListGamesResult;
import result.UserResult;

import java.util.Scanner;
public class Client {
    private static String authToken;
    private static String username;
    private static final Scanner READER = new Scanner(System.in);
    private static final ServerFacade SERVER_FACADE = new ServerFacade(8080);

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
            SERVER_FACADE.joinGame(authToken, color, gameID);

            System.out.println("Success!");

            //Later add functionality

            //Prints a basic chess board
            ChessBoard board = new ChessBoard();
            board.resetBoard();

            ChessBoardPrinter printer = new ChessBoardPrinter(board, color);
            System.out.println(printer.printBoard());
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
        if(gameID == -1) { return; }

        // Later add functionality

        //Prints a basic chess board
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        ChessBoardPrinter printer = new ChessBoardPrinter(board, "white");
        System.out.println(printer.printBoard());
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
}
