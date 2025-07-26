package Classes;

import javafx.application.Application;
import Controllers.LoginPage;


public class Main {
    public static void main(String[] args) {
        Application.launch(LoginPage.class, args);
        User currentUser = Session.getCurrentUser();
        System.out.println(currentUser.toString());
    }
}
