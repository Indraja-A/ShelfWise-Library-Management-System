public class Admin {

    private final String username = "admin";
    private final String password = "ShelfWise@123";

    public boolean login(String enteredUsername, String enteredPassword) {
        return username.equals(enteredUsername)
                && password.equals(enteredPassword);
    }
}
