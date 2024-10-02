package com.master;

import org.zeromq.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class masterNode {
    public static void main(String[] args) throws IOException {

        ZMQ.Context context = ZMQ.context(1); //cerate the context

        ZMQ.Socket socket = context.socket(SocketType.PUSH); //create the socket PUSH that send chunks to workers
        socket.bind("tcp://localhost:5555");

        ZMQ.Socket resulter = context.socket(SocketType.PULL); //create the resulter PULL that gets the results from the reducers
        resulter.bind("tcp://*:5558");

        String file = "C:\\Users\\user\\IdeaProjects\\MapReduce\\src\\main\\java\\inputfile.txt"; //path to the file

        Scanner scanner = new Scanner(System.in); //scanner for the input


        try {
                String content = new String(Files.readAllBytes(Paths.get(file))); //get all the content from the file by converting bytes to the string
                content = content.replace(".", "");
                content = content.replace(",", "");
                content = content.toLowerCase(); //clean the file
//                content = content.replace(" the ", " ");
//                content = content.replace(" to ", " ");
//                content = content.replace(" in ", " ");
//                content = content.replace(" is ", " ");
//                content = content.replace(" and ", " ");
//                content = content.replace(" for ", " ");
//                content = content.replace(" of ", " ");
//                content = content.replace(" or ", " ");
//                content = content.replace(" a ", " ");
                List<String> chunks = splitToChunks(content, 10); //split to 10 chunks and add them to the List

                for (String chunk : chunks){ //send the chuks to the mappers by iterating through the List
                    socket.send(chunk); //send the chunk to a mapper
                    System.out.println("Chunk sent: " + new String(chunk.getBytes()));
                    Thread.sleep(200); //sleep to prevent packet loss
                }

                for (int i = 0; i < 3; i++){
                    socket.send("STOP"); //send stop to 3 mappers
                    System.out.println("STOP sent");
                }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        Map<String, Integer> finalResult = new HashMap<>(); //map of results from the reducers for easy output

        int receivedStops = 0;
        int expectedStops = 2; //number of reducers

        List<String> reducerResults = new ArrayList<>(); //list to store the raw results

        while (receivedStops<=expectedStops) { //work until both of the reducers send the STOP signal

            String result = resulter.recvStr(0); //get the results from reducers

            if ("STOP".equals(result)){ //if STOP gets received increment stops
                receivedStops++;
                System.out.println("Receeived Stop Signal " + receivedStops);
                if (receivedStops == expectedStops) break; //break if gets 2 STOP signals
            }


            reducerResults.add(result); //add the results


        }

        for (String result : reducerResults){ //iterate through the List
            String[] spread = result.split(" "); //divide it to 2 values String and Int
            if (spread.length < 2){ //skip the excess STOP message or invalid message if there any
                System.out.println("Invalid result: " + result);
                continue; //continue
            }
            String word = spread[0].replace(":",""); //thats a key with removed : from the end of the word
            int count = Integer.parseInt(spread[1]); //parse the value to int
            finalResult.put(word, finalResult.getOrDefault(word, 0) + count); // put the results to the map from the list
        }

        System.out.println("Now provide the output you want to see: ");
        System.out.println("1: To show the each word" + "\n" + "2: To see how many words in total" + "\n" + "3: To see a specific word");

            int choice = scanner.nextInt();
            if (choice == 1) {
                for (Map.Entry<String, Integer> entry : finalResult.entrySet()) {
                    System.out.println("Word: " + entry.getKey() + " is met " + entry.getValue() + " times"); //iterate and show all the words separately
                }
            } else if (choice == 2) {
                int words = 0;
                for (int sum : finalResult.values()) {
                    words += sum;
                }
                System.out.println("There are " + words + " words!"); //iterate and show all the words
            } else if (choice == 3) {
                scanner.nextLine();
                System.out.println("Please enter the keyword: ");
                String keyword = scanner.nextLine();
                System.out.println(keyword);
                keyword = keyword.toLowerCase().trim();
                for (Map.Entry<String, Integer> entry : finalResult.entrySet()) {
                    if (entry.getKey().equals(keyword)) {
                        System.out.println("Word: " + entry.getKey() + " is met " + entry.getValue() + " times"); //iterate and show the particular word
                    }
                }
            }


        socket.close();
        context.term();
    }

    public static List<String> splitToChunks(String input, int numOfChunks){ //function to split the document to chunks

        String[] words = input.split("\\s+"); //split the input string into individual words using whitespace as the delimiter
        int totalWords = words.length; //calculate the total number of words in the document
        int chunkSize = totalWords / numOfChunks; //calculate how many words should go into each chunk

        List<String> chunks = new ArrayList<>();  //list to store the resulting chunks
        StringBuilder chunk = new StringBuilder(); //stringBuilder to accumulate words for the current chunk


        for (int i = 0; i < words.length; i++) { //distribute the words across the chunks
            chunk.append(words[i]).append(" ");


            if ((i + 1) % chunkSize == 0 || i == words.length - 1) { //add chunk to the list when it's full or at the last word
                chunks.add(chunk.toString().trim());
                chunk.setLength(0);  //clear the StringBuilder for the next chunk
            }
        }

        return chunks;
    }

}


