package com.router;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

public class Router {
    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);


        Socket router = context.socket(ZMQ.REP);
        router.bind("tcp://*:5556");


        Socket dealer = context.socket(ZMQ.PUSH);
        dealer.bind("tcp://*:5557");



        router.connect("tcp://localhost:5557");
        System.out.println("Router is running");

        ZMQ.Poller poller = context.poller(2);
        poller.register(router, ZMQ.Poller.POLLIN);
        poller.register(dealer, ZMQ.Poller.POLLIN);

       while (true) {
                String message = router.recvStr(0);
                System.out.println("Received " + message);
                router.send("ACK");
                System.out.println("Sent ACK");
                dealer.send(message);
                System.out.println("DEALER SENT " + new String(message));
       }
    }
}
