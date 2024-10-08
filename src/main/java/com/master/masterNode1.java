//There are 3 masterNodes and most of the code is the same so I have comments on masterNode.java please check there
//I will add additional comments here where needed

package com.master;

import org.zeromq.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class masterNode1 {
    public static void main(String[] args) throws IOException {

        ZMQ.Context context = ZMQ.context(1);

        ZMQ.Socket socket = context.socket(SocketType.PUSH);
        socket.bind("tcp://localhost:5555");

        ZMQ.Socket resulter = context.socket(SocketType.PULL);
        resulter.bind("tcp://*:5558");

        Scanner scanner = new Scanner(System.in);

        System.out.println("Please specify the file or enter path to the file: ");

        String file = scanner.nextLine();

        System.out.println("Now, we need to start worker/mapper nodes");
        System.out.println("Please, specify how many worker nodes do you want to start?");

        int mappers = scanner.nextInt(); //Int for choosing ammount of mappers

        for (int i = 1; i<=mappers;i++){ //start mappers using processbuilder in the separate cmds
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "cmd.exe", "/c", "start", "java", "-cp", "src/main/java;src/lib/jeromq-0.6.0.jar", "com.worker.workerNode", String.valueOf(i)
                );
                processBuilder.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Now, we need to start reducer nodes");
        System.out.println("Please, specify how many reducer nodes do you want to start?");

        int reducers = scanner.nextInt(); //Int for choosing ammount of reducers

        for (int i = 1; i<=reducers;i++){ //start reducers using processbuilder in the separate cmds
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "cmd.exe", "/c", "start", "java", "-cp", "src/main/java;src/lib/jeromq-0.6.0.jar", "com.reducer.reducerNode", String.valueOf(i)
                );
                processBuilder.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Now, we need to start the router");


        try { //start the router using processbuilder in the separate cmd
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "cmd.exe", "/c", "start", "java", "-cp", "src/main/java;src/lib/jeromq-0.6.0.jar", "com.router.Router"
                );
               processBuilder.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }



        System.out.println("Now enter 'send' to send chunks to workers/mappers");
        String send = scanner.nextLine();
        send = send.toLowerCase();

        while (!send.equals("send")){ //check if the input is 'send'
            System.out.println("Now enter 'send' to send chunks to workers/mappers");
            send = scanner.nextLine();
            send = send.toLowerCase();
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(file)));
            content = content.replace(".", "");
            content = content.replace(",", "");
            content = content.toLowerCase();
//            content = content.replace(" the ", " ");
//            content = content.replace(" to ", " ");
//            content = content.replace(" in ", " ");
//            content = content.replace(" is ", " ");
//            content = content.replace(" and ", " ");
//            content = content.replace(" for ", " ");
//            content = content.replace(" of ", " ");
//            content = content.replace(" or ", " ");
//            content = content.replace(" a ", " ");
            List<String> chunks = splitToChunks(content, 10);

            for (String chunk : chunks){
                socket.send(chunk);
                System.out.println("Chunk sent: " + new String(chunk.getBytes()));
                Thread.sleep(200);
            }

            for (int i = 0; i < mappers; i++){
                socket.send("STOP");
                System.out.println("STOP sent");
                Thread.sleep(50);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        Map<String, Integer> finalResult = new HashMap<>();

        int receivedStops = 0;

        List<String> reducerResults = new ArrayList<>();

        while (receivedStops<=reducers) {

            String result = resulter.recvStr(0);

            if ("STOP".equals(result)){
                receivedStops++;
                System.out.println("Receeived Stop Signal " + receivedStops);
                if (receivedStops == reducers) break;
            }


            reducerResults.add(result);
        }

        for (String result : reducerResults){
            String[] spread = result.split(" ");
            if (spread.length < 2){
                System.out.println("Invalid result: " + result);
                continue;
            }
            String word = spread[0].replace(":","");
            int count = Integer.parseInt(spread[1]);
            finalResult.put(word, finalResult.getOrDefault(word, 0) + count);
        }


        System.out.println("1: To show the each word" + "\n" + "2: To see how many words in total" + "\n" + "3: To see a specific word " + "\n" + "Or 0 to stop the program");
        int choice = scanner.nextInt();


        while (choice != 0) {

            if (choice == 1) {
                System.out.println();
                for (Map.Entry<String, Integer> entry : finalResult.entrySet()) {
                    System.out.println("Word: " + entry.getKey() + " is met " + entry.getValue() + " times");
                }
                System.out.println();
                System.out.println("Now provide the output you want to see: ");
                System.out.println("1: To show the each word" + "\n" + "2: To see how many words in total" + "\n" + "3: To see a specific word " + "\n" + "Or 0 to stop the program");
            } else if (choice == 2) {
                int words = 0;
                for (int sum : finalResult.values()) {
                    words += sum;
                }
                System.out.println();
                System.out.println("There are " + words + " words!");
                System.out.println();
                System.out.println("Now provide the output you want to see: ");
                System.out.println("1: To show the each word" + "\n" + "2: To see how many words in total" + "\n" + "3: To see a specific word " + "\n" + "Or 0 to stop the program");
            } else if (choice == 3) {
                scanner.nextLine();
                System.out.println("Please enter the keyword: ");
                String keyword = scanner.nextLine();
                keyword = keyword.toLowerCase().trim();
                System.out.println();
                for (Map.Entry<String, Integer> entry : finalResult.entrySet()) {
                    if (entry.getKey().equals(keyword)) {
                        System.out.println("Word: " + entry.getKey() + " is met " + entry.getValue() + " times");
                    }
                }
                System.out.println();
                System.out.println("Now provide the output you want to see: ");
                System.out.println("1: To show the each word" + "\n" + "2: To see how many words in total" + "\n" + "3: To see a specific word " + "\n" + "Or 0 to stop the program");
            }else {
                System.out.println("Now provide the output you want to see: ");
                System.out.println("1: To show the each word" + "\n" + "2: To see how many words in total" + "\n" + "3: To see a specific word " + "\n" + "Or 0 to stop the program");
            }

            choice = scanner.nextInt();
        }

        socket.close();
        context.term();
    }

    public static List<String> splitToChunks(String input, int numOfChunks){

        String[] words = input.split("\\s+");
        int totalWords = words.length;
        int chunkSize = totalWords / numOfChunks;

        List<String> chunks = new ArrayList<>();
        StringBuilder chunk = new StringBuilder();

        // Distribute the words across the chunks
        for (int i = 0; i < words.length; i++) {
            chunk.append(words[i]).append(" ");

            // Add chunk to the list when it's full or at the last word
            if ((i + 1) % chunkSize == 0 || i == words.length - 1) {
                chunks.add(chunk.toString().trim());
                chunk.setLength(0);  // Clear the StringBuilder for the next chunk
            }
        }

        return chunks;
    }


}


