package com.reducer;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class reducerNode {
    public static void main(String[] args) throws Exception {
        ZMQ.Context context = ZMQ.context(5);
        ZMQ.Socket receiver = context.socket(ZMQ.PULL);
        receiver.connect("tcp://localhost:5557");

        ZMQ.Socket responder = context.socket(SocketType.PUSH);
        responder.connect("tcp://localhost:5558");


        if (args.length == 0){
            System.out.println("No identification was provided by the master!");
            String id = "DefaultWorker";
            System.out.println("Reducer with id: " + id + " is running");
        }else {
            String id = args[0];
            System.out.println("Reducer with id: " + id + " is running");
        }

        Map<String, Integer> finalWordCount = new HashMap<>();


        while (true) {
            String result = receiver.recvStr(0);
            System.out.println("Received message");
            if (result.equals("STOP")) {
                receiver.close();
                System.out.println("Received STOP");
                for (Map.Entry<String, Integer> entry : finalWordCount.entrySet()) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                    responder.send(entry.getKey() + ": " + entry.getValue());
                    System.out.println("Sent map");
                    Thread.sleep(50);
                }
                responder.send("STOP");
                System.out.println("Sent STOP");
                break;
            }



            String[] parts = result.split(" ");
            String word = parts[0];
            int count = Integer.parseInt(parts[1]);

            if (finalWordCount.containsKey(word)) {
                finalWordCount.put(word, finalWordCount.get(word) + count);
            } else {
                finalWordCount.put(word, count);
            }


        }

        receiver.close();
        context.term();
    }
}