package CPUSimulation;
import java.util.Random;

public class HelloWorld {
	
	public static int random(int seed) {
		Random randomGenerator = new Random();
		randomGenerator.setSeed(seed);
		for(int i=0;i<20;i++)
			System.out.println(randomGenerator.nextFloat()%1);
		return 1;
	}

	public static void main(String[] args) {
		HelloWorld.random(5);
	}

}
