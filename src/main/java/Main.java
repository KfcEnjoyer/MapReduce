
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {



        System.out.println("Hello user!");
        System.out.println("This program will allow you count the words using distributed systems");
        System.out.println("To start, first, we need to start the master node");
        System.out.println("Press 1 to start the masterNode1 or 2 to start the masternode2 or 0 to stop: ");
        Scanner scanner = new Scanner(System.in);
        int startMaster = scanner.nextInt();
        while (startMaster != 0) {
            if (startMaster == 1) {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "cmd.exe", "/c", "start", "java", "-cp", "src/main/java;src/lib/jeromq-0.6.0.jar", "com.master.masterNode1"
                );
                processBuilder.start();
            } else if (startMaster == 2) {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "cmd.exe", "/c", "start", "java", "-cp", "src/main/java;src/lib/jeromq-0.6.0.jar", "com.master.masterNode2"
                );
                processBuilder.start();
            } else {
                System.out.println("Press 1 to start the masterNode1 or 2 to start the masternode2 or 0 to stop: ");
            }
            startMaster = scanner.nextInt();
        }

        System.out.println("Master Node started!");


    }
}
