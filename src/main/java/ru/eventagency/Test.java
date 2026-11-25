import java.util.Scanner;

abstract class Repository123{
	abstract void readString();	
}

class UserRepository extends Repository123{
	
	@Override
 	public void readString(){
		System.out.print("Введите строку: ");
		Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        System.out.println("Вы ввели: " + input);
	} 
}

public class Test {
	public static void main(String[] args){
		UserRepository userRepository = new UserRepository();
		userRepository.readString();
	}
} 