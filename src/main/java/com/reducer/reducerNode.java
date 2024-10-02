package com.reducer;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class reducerNode {
    public static void main(String[] args) throws Exception {
        ZMQ.Context context = ZMQ.context(5); //create the context
        ZMQ.Socket receiver = context.socket(ZMQ.PULL); //create the receiver with PULL connection
        receiver.connect("tcp://localhost:5557"); //connect to the dealer

        ZMQ.Socket responder = context.socket(SocketType.PUSH);
        responder.connect("tcp://localhost:5558"); //connect to the master


        if (args.length == 0){ //checking if master assigned the id to the worker as asrg
            System.out.println("No identification was provided by the master!");
            String id = "DefaultReducer";
            System.out.println("Reducer with id: " + id + " is running"); //if not then assign DefaultReducer
        }else {
            String id = args[0];
            System.out.println("Reducer with id: " + id + " is running"); //if yes then assign the id
        }

        Map<String, Integer> finalWordCount = new HashMap<>(); //create the map to store the results


        while (true) { //listen for the results from the dealer
            String result = receiver.recvStr(0); //get the results
            System.out.println("Received message");
            if (result.equals("STOP")) { //check for the STOP message
                receiver.close(); //close the reciver for each reducer after it gets the STOP message
                System.out.println("Received STOP");
                for (Map.Entry<String, Integer> entry : finalWordCount.entrySet()) { //send the results to the master as a String via iterating through the map
//                    System.out.println(entry.getKey() + ": " + entry.getValue());
                    responder.send(entry.getKey() + ": " + entry.getValue()); //send the results to the master as a String via iterating through the map
                    System.out.println("Sent map");
                    Thread.sleep(50); //sleep for prevention of packet loss
                }
                responder.send("STOP"); //send the STOP to the master
                System.out.println("Sent STOP");
                break; //break the loop
            }



            String[] parts = result.split(" "); //split the results from the mappers to the array of strings divided by space
            String word = parts[0]; //this are the words(key) from the map
            int count = Integer.parseInt(parts[1]); //this is the count(value) from the map

            if (finalWordCount.containsKey(word)) {
                finalWordCount.put(word, finalWordCount.get(word) + count); //check if the map has the word from the previous result and add the previous count of the word to the current count
            } else {
                finalWordCount.put(word, count); //if not then put the current count
            }


        }

        receiver.close();
        context.term();
    }
}