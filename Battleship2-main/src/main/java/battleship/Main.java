package battleship;

public class Main
{
	/**
	 * Main.
	 *
	 * @param args the args
	 */
	public static void main(String[] args)
	{
		DatabaseManager.initializeDatabase();
		Tasks.chooseLanguage();
		System.out.println(Messages.get("app.title"));
		Tasks.menu();
	}
}