package com.master;

import org.zeromq.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class masterNode {
    public static void main(String[] args) throws IOException {

        ZMQ.Context context = ZMQ.context(1);

        ZMQ.Socket socket = context.socket(SocketType.PUSH);
        socket.bind("tcp://localhost:5555");

        ZMQ.Socket resulter = context.socket(SocketType.PULL);
        resulter.bind("tcp://*:5558");

        String file = "C:\\Users\\user\\IdeaProjects\\MapReduce\\src\\main\\java\\inputfile.txt";

        Scanner scanner = new Scanner(System.in);


        try {
                String content = new String(Files.readAllBytes(Paths.get(file)));
                content = content.replace(".", "");
                content = content.replace(",", "");
                content = content.toLowerCase();
//                content = content.replace(" the ", " ");
//                content = content.replace(" to ", " ");
//                content = content.replace(" in ", " ");
//                content = content.replace(" is ", " ");
//                content = content.replace(" and ", " ");
//                content = content.replace(" for ", " ");
//                content = content.replace(" of ", " ");
//                content = content.replace(" or ", " ");
//                content = content.replace(" a ", " ");
                List<String> chunks = splitToChunks(content, 10);

                for (String chunk : chunks){
                    socket.send(chunk);
                    System.out.println("Chunk sent: " + new String(chunk.getBytes()));
                    Thread.sleep(200);
                }

                for (int i = 0; i < 3; i++){
                    socket.send("STOP");
                    System.out.println("STOP sent");
                }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        Map<String, Integer> finalResult = new HashMap<>();

        int receivedStops = 0;
        int expectedStops = 2;

        List<String> reducerResults = new ArrayList<>();

        while (receivedStops<=expectedStops) {

            String result = resulter.recvStr(0);

            if ("STOP".equals(result)){
                receivedStops++;
                System.out.println("Receeived Stop Signal " + receivedStops);
                if (receivedStops == expectedStops) break;
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

        System.out.println("Now provide the output you want to see: ");
        System.out.println("1: To show the each word" + "\n" + "2: To see how many words in total" + "\n" + "3: To see a specific word");

            int choice = scanner.nextInt();
            if (choice == 1) {
                for (Map.Entry<String, Integer> entry : finalResult.entrySet()) {
                    System.out.println("Word: " + entry.getKey() + " is met " + entry.getValue() + " times");
                }
            } else if (choice == 2) {
                int words = 0;
                for (int sum : finalResult.values()) {
                    words += sum;
                }
                System.out.println("There are " + words + " words!");
            } else if (choice == 3) {
                scanner.nextLine();
                System.out.println("Please enter the keyword: ");
                String keyword = scanner.nextLine();
                System.out.println(keyword);
                keyword = keyword.toLowerCase().trim();
                for (Map.Entry<String, Integer> entry : finalResult.entrySet()) {
                    if (entry.getKey().equals(keyword)) {
                        System.out.println("Word: " + entry.getKey() + " is met " + entry.getValue() + " times");
                    }
                }
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


