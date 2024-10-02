package com.router;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

public class Router {
    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1); //create ZMQ context


        Socket router = context.socket(ZMQ.REP); //create router with REP connection
        router.bind("tcp://*:5556"); //bind the connection


        Socket dealer = context.socket(ZMQ.PUSH); //create dealer with PUSH connection
        dealer.bind("tcp://*:5557"); //bind the connection



//        router.connect("tcp://localhost:5557");
        System.out.println("Router is running"); //Acknowledge that router is running

//        ZMQ.Poller poller = context.poller(2);
//        poller.register(router, ZMQ.Poller.POLLIN);
//        poller.register(dealer, ZMQ.Poller.POLLIN);

       while (true) {
                String message = router.recvStr(0); //listen for the result from mappers
                System.out.println("Received " + message); //print the messages
                router.send("ACK"); //send the acknoledgment to the mappers
                System.out.println("Sent ACK");
                dealer.send(message); //send the result to the reducers
                System.out.println("DEALER SENT " + new String(message));
       }
    }
}
