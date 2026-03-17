package client.ui;

import java.util.Scanner;
public class Client {

    public static void main(String[] args) {

        Scanner reader = new Scanner(System.in);
        while(true) {
            System.out.println("""
                    ♕ Welcome to the 240 Chess Server ♕
                    
                    What would you like to do?
                    1 - Register
                    2 - Login
                    3 - Help
                    4 - Quit""");
            int i = reader.nextInt();

            if(i == 3) {
                System.out.println("""
                        Register allows you to create a new account
                        Login prompts you enter your username and password to login
                        Quit exits the program""");
            }
            if(i == 4) { break; }
        }
    }
}
