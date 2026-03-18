package client.ui;

import java.util.Scanner;
public class Client {
    private static String authToken;
    private static String username;

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

            } else if(choice == 2) { // Login
                //get AuthToken and username

                postLoginUI();
            } else if(choice == 3) { // Help
                System.out.println("""
                        Register - Allows you to create a new account
                        Login - Prompts you enter your username and password to login
                        Quit - Exits the program\n""");
            } else if(choice == 4) { break; } // Quit
        }
    }

    private static void postLoginUI() {
        while(true) {
            System.out.printf("""
                    ♕ Hello %s ♕
                    
                    What would you like to do?
                    1 - Play Game
                    2 - Create Game
                    3 - List Games
                    4 - Observe Game
                    5 - Help
                    6 - Logout%n""", username);

            int choice = getInput(6);

            if(choice == 1) { // Play Game

            } else if(choice == 2) { // Create Game

            } else if(choice == 3) { // List Games

            } else if(choice == 4) { // Observe Game

            } else if(choice == 5) { // Help
                System.out.println("""
                        Play Game - Allows you to join a chess game with an open spot
                        Create Game - Creates a new chess game
                        List Games - Lists all games in the database
                        Observe Game - Allows you to observe a game
                        Logout - Logs you out and returns you to the main prompt\n""");
            } else if(choice == 6) { // Logout
                //logout
                break;
            }
        }
    }

    private static int getInput(int numOfChoices) {
        Scanner reader = new Scanner(System.in);

        while(true) {
            try {
                int choice = Integer.parseInt(reader.nextLine());
                if(choice < 1 || choice > numOfChoices) {
                    System.out.printf("Invalid number: Please input a number from 1 - %d\n", numOfChoices);
                } else {
                    return choice;
                }
            } catch(Exception e) {
                System.out.printf("Invalid number: Please input a number from 1 - %d\n", numOfChoices);
            }
        }
    }
}
