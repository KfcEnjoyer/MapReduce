package com.master;

import org.zeromq.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class masterNode2 {
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
        List<Process> mappersList = new ArrayList<>();

        int mappers = scanner.nextInt();

        for (int i = 1; i<=mappers;i++){
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "java", "-cp", "src/main/java;src/lib/jeromq-0.6.0.jar", "com.worker.workerNode", String.valueOf(i)
                );
                processBuilder.redirectOutput(new File("worker" + i + ".log"));
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Now, we need to start reducer nodes");
        System.out.println("Please, specify how many reducer nodes do you want to start?");
        List<Process> reducersList = new ArrayList<>();

        int reducers = scanner.nextInt();

        for (int i = 1; i<=reducers;i++){
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "java", "-cp", "src/main/java;src/lib/jeromq-0.6.0.jar", "com.reducer.reducerNode", String.valueOf(i)
                );
                processBuilder.redirectOutput(new File("reducer" + i + ".log"));
                processBuilder.redirectErrorStream(true);
                processBuilder.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Now, we need to start the router");
        Process router = null;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java", "-cp", "src/main/java;src/lib/jeromq-0.6.0.jar", "com.router.Router"
            );
            processBuilder.redirectOutput(new File("router" + ".log"));
            processBuilder.redirectErrorStream(true);
            router = processBuilder.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }



        System.out.println("Now enter 'send' to send chunks to workers/mappers");
        String send = scanner.nextLine();
        send = send.toLowerCase();

        while (!send.equals("send")){
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

        killProcesses(mappersList);
        killProcesses(reducersList);
        killProcess(router);

        for (String result : reducerResults){
            String[] spread = result.split(" ");
            if (spread.length < 2){
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
                System.out.println("1: To show the each word" + "\n" + "2: To see how many words in total" + "\n" + "3: To see a specific word");
            } else if (choice == 2) {
                int words = 0;
                for (int sum : finalResult.values()) {
                    words += sum;
                }
                System.out.println();
                System.out.println("There are " + words + " words!");
                System.out.println();
                System.out.println("Now provide the output you want to see: ");
                System.out.println("1: To show the each word" + "\n" + "2: To see how many words in total" + "\n" + "3: To see a specific word");
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
                System.out.println("1: To show the each word" + "\n" + "2: To see how many words in total" + "\n" + "3: To see a specific word");
            }else {
                System.out.println("Now provide the output you want to see: ");
                System.out.println("1: To show the each word" + "\n" + "2: To see how many words in total" + "\n" + "3: To see a specific word");
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

    public static void killProcess(Process process) {
        if (process != null && process.isAlive()) {
            process.destroy();
            System.out.println("Process destroyed");
        }
    }

    public static void killProcesses(List<Process> processes) {
        for (Process process : processes) {
            if (process != null && process.isAlive()) {
                process.destroy();
                System.out.println("Process destroyed");
            }
        }
    }



}


