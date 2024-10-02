package com.worker;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

public class workerNode {
    public static void main(String[] args) throws InterruptedException {
        ZMQ.Context context = ZMQ.context(3);  //creating the ZMQ context with 3 threads
        ZMQ.Socket receiver = context.socket(SocketType.PULL); //create the receiver with PULL connection
        receiver.connect("tcp://localhost:5555"); //connect to the masterNode's PUSH bind

        ZMQ.Socket sender = context.socket(SocketType.REQ); //create the sender with REQ connection
        sender.connect("tcp://localhost:5556"); //connect to the router's REP bind

        if (args.length == 0){  //checking if master assigned the id to the worker as asrg
            System.out.println("No identification was provided by the master!");
            String id = "DefaultWorker";
            System.out.println("Worker with id: " + id + " is running"); //if not then assign DefaultWorker
        }else {
            String id = args[0];
            System.out.println("Worker with id: " + id + " is running"); //if yes then assign the id
        }


        while (true){ //receive chunks from the master

           String content = receiver.recvStr();

           System.out.println("Received: " + content); //print received chunks from the master

           Map<String, Integer> wordCount = countWords(content); //create the map to store word: count using a function

            if ("STOP".equals(content)){ //check for the STOP
                sender.send("STOP"); //send stop to router
                System.out.println("STOP Sent");
                context.close(); //close connection
                receiver.close();
                sender.close();
                break; //break the loop
            }

           for (Map.Entry<String, Integer> entry : wordCount.entrySet()){ //send the results to the router by iterating through the map
               sender.send((entry.getKey() + " " + entry.getValue()).getBytes(ZMQ.CHARSET), 0); //send all the results as a string and converting int to bytes
               System.out.println("Sent result");
               String ack = sender.recvStr(); //get the acknowledge that router got the result
               System.out.println("Received ACK: " + ack);
               Thread.sleep(200); //sleep for preevnting loss of the packets
           }


       }

    }

    public static Map<String, Integer> countWords(String content){ //function to count words that takes String as a parameter
        String[] words = content.split(" "); //split the string into array of strings splitting by space. It will contain the words individually
        Map<String, Integer> wordMap = new HashMap<>(); //create a new map that will store the words and counts and will be returned
        for (String word : words){ //iterate through the array of strings
            if (wordMap.containsKey(word)){ //check if the map already contains the word
                wordMap.put(word, wordMap.get(word)+1); //if yes then add the value by previous value of the word
            }else {
                wordMap.put(word, 1); //if not just put 1 as count
            }
        }

        return wordMap; //return the map
    }
}
