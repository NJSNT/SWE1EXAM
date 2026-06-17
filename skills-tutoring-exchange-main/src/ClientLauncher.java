/**
 * Fat-JAR entry point for the client.
 * Delegates to Main (JavaFX Application) without a package import
 * because Main lives in the default package.
 */
public class ClientLauncher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
