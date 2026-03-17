package client.ui;

import java.util.Scanner;
public class Client {

    public static void main(String[] args) {
        while(true) {
            System.out.println("""
                    ♕ Welcome to the 240 Chess Program ♕
                    
                    What would you like to do?
                    1 - Register
                    2 - Login
                    3 - Help
                    4 - Quit""");

            int choice = getInput(1, 4);

            if(choice == 3) {
                System.out.println("""
                        Register allows you to create a new account
                        Login prompts you enter your username and password to login
                        Quit exits the program\n""");
            }
            else if(choice == 4) { break; }
        }
    }

    private static int getInput(int min, int max) {
        Scanner reader = new Scanner(System.in);

        while(true) {
            try {
                int choice = Integer.parseInt(reader.nextLine());
                if (choice < min || choice > max) {
                    System.out.println("Invalid number: Please input a number from 1 - 4\n");
                } else {
                    return choice;
                }
            } catch (Exception e) {
                System.out.println("Invalid number: Please input a number from 1 - 4\n");
            }
        }
    }
}
