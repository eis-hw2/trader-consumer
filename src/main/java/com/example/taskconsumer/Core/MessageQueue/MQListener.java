package com.example.taskconsumer.Core.MessageQueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MQListener {
    private final static String QUEUE_NAME = "FutureTask";
    private final static String MQ_HOST = "47.106.8.44";
    //private final static Logger logger = LoggerFactory.getLogger("MQListener");
    private final static boolean AUTO_ACK = false;
    private final static String EXCHANGE = "Cancel";

    private final static ConnectionFactory factory = getFactory();
    private final static Connection connection = getConnection(factory);

    public void listenCreate(TaskConsumer taskConsumer) throws Exception{

        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        channel.basicConsume(QUEUE_NAME, AUTO_ACK, taskConsumer.consume(channel), consumerTag -> { });
    }

    public void listenCancel(TaskConsumer taskConsumer)throws Exception{
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE, "");

        channel.basicConsume(queueName, AUTO_ACK, taskConsumer.consume(channel), consumerTag -> { });
    }

    private static ConnectionFactory getFactory(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(MQ_HOST);
        return factory;
    }

    private static Connection getConnection(ConnectionFactory factory){
        try {
            Connection connection = factory.newConnection();
            return connection;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
