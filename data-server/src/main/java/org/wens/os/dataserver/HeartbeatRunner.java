package org.wens.os.dataserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.stereotype.Component;
import org.wens.os.common.queue.MessageQueue;
import org.wens.os.common.queue.RedisMessageQueue;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class HeartbeatRunner implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private JedisPool jedisPool ;

    @Value("${server.address}:${server.port}")
    private String listenAddress ;



    @Override
    public void run(String... args) throws Exception {
        MessageQueue messageQueue = new RedisMessageQueue("dataServer" , jedisPool );
        messageQueue.start();
        new Thread(()->{
            AtomicBoolean stopped = new AtomicBoolean(false);
            Runtime.getRuntime().addShutdownHook(new Thread(()->stopped.set(true)));

            while ( !stopped.get() ){
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(2));messageQueue.send(listenAddress.getBytes());
                }catch (Throwable t ){
                    log.error("send heartbeat fail" , t );
                }
            }

        },"heartbeat-thread").start();



    }
}
