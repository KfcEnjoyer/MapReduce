

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class test {
    public static void main(String[] args) throws InterruptedException {
        ZMQ.Context context = ZMQ.context(3);
        ZMQ.Socket receiver = context.socket(SocketType.PULL);
        receiver.connect("tcp://localhost:5555");

        ZMQ.Socket sender = context.socket(SocketType.REQ);
        sender.connect("tcp://localhost:5556");


        String processName = ManagementFactory.getRuntimeMXBean().getName();
        String pid = processName.split("@")[0];

        System.out.println("Worker with PID: " + pid + " Running");
        while (true){

            String content = receiver.recvStr();

            System.out.println("Worker: " + Thread.currentThread().getId());
            System.out.println("Received: " + content);

            Map<String, Integer> wordCount = countWords(content);

            if ("STOP".equals(content)){
                sender.send("STOP");
                System.out.println("STOP Sent");
                receiver.close();
                break;
            }

            for (Map.Entry<String, Integer> entry : wordCount.entrySet()){
                sender.send((entry.getKey() + " " + entry.getValue()).getBytes(ZMQ.CHARSET), 0);
                System.out.println("Sent result");
                String ack = sender.recvStr();
                System.out.println("Received ACK: " + ack);
                Thread.sleep(200);
            }


        }
        context.close();
        sender.close();
    }

    public static Map<String, Integer> countWords(String content){
        String[] words = content.split(" ");
        Map<String, Integer> wordMap = new HashMap<>();
        for (String word : words){
            if (wordMap.containsKey(word)){
                wordMap.put(word, wordMap.get(word)+1);
            }else {
                wordMap.put(word, 1);
            }
        }

        return wordMap;
    }
}
