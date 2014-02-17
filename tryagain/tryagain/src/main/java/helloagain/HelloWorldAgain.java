package helloagain;

public class HelloWorldAgain {

	
	public static void main(String[] args) {
		String defaultWord = "World";
		if(args != null && args.length != 0) {
			defaultWord = args[0];
		}
		System.out.println("Hello " + defaultWord);
	}

}