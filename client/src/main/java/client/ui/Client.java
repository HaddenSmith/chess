package client.ui;

import result.CreateGameResult;
import result.GameSummary;
import result.ListGamesResult;
import result.UserResult;

import java.util.Scanner;
public class Client {
    private static String authToken;
    private static String username;
    private static final Scanner reader = new Scanner(System.in);
    private static final ServerFacade serverFacade = new ServerFacade(8080);

    public static void main(String[] args) throws Exception{
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
                        Quit - Exits the program\n""");
            } else if(choice == 4) { break; } // Quit
        }
    }

    private static void postLoginUI() throws Exception{
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
                //joinGame();
            } else if(choice == 2) { // Create Game
                createGame();
            } else if(choice == 3) { // List Games
                listGames();
            } else if(choice == 4) { // Observe Game

            } else if(choice == 5) { // Help
                System.out.println("""
                        Play Game - Allows you to join a chess game with an open spot
                        Create Game - Creates a new chess game
                        List Games - Lists all games in the database
                        Observe Game - Allows you to observe a game
                        Logout - Logs you out and returns you to the main prompt\n""");
            } else if(choice == 6) { // Logout
                logout();
                break;
            }
        }
    }

    private static int getInput(int numOfChoices) {
        while(true) {
            try {
                int choice = Integer.parseInt(reader.nextLine());
                if(choice < 1 || choice > numOfChoices) {
                    System.out.printf("Invalid number: Please input a number from 1 - %d%n", numOfChoices);
                } else {
                    return choice;
                }
            } catch(Exception e) {
                System.out.printf("Invalid number: Please input a number from 1 - %d%n", numOfChoices);
            }
        }
    }

    private static void register() throws Exception{
        System.out.println("Enter in the username for the new account:");
        String entered_username = reader.nextLine();
        System.out.println("Enter in the password for the new account:");
        String password = reader.nextLine();
        System.out.println("Enter in the email for the new account:");
        String email = reader.nextLine();

        try {
            UserResult result = serverFacade.register(entered_username, password, email);
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
        String entered_username = reader.nextLine();
        System.out.println("Enter in the password:");
        String entered_password = reader.nextLine();

        try {
            UserResult result = serverFacade.login(entered_username, entered_password);
            authToken = result.authToken();
            username = result.username();

            System.out.println("Success!");

            postLoginUI();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            username = null;
        }
    }

    /*private static void joinGame() {
        System.out.println("Enter in the color you wish to join as:");
        String color = reader.nextLine();
        System.out.println("Enter in the game name:");
        String gameID = reader.nextLine();

        try {
            UserResult result = serverFacade.joinGame(authToken, color, gameID);
            authToken = result.authToken();
            username = result.username();

            System.out.println("Success!");

            postLoginUI();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            username = null;
        }
    }*/

    public static void createGame() throws Exception{
        System.out.println("Enter in the name of the game");
        String gameName = reader.nextLine();

        try {
            serverFacade.createGame(authToken, gameName);

            System.out.println("Success!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void listGames() {
        try {
            ListGamesResult result = serverFacade.listGames(authToken);
            for (GameSummary game : result.games()) {
                System.out.println(game);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void logout() {
        try {
            serverFacade.logout(authToken);
            username = null;
            authToken = null;

            System.out.println("Success!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
