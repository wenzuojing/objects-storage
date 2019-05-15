package org.wens.os.apiserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wens.os.common.queue.MessageListener;
import org.wens.os.common.queue.MessageQueue;
import org.wens.os.common.queue.RedisMessageQueue;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author wens
 */
@Service
public class ActiveDataServerService implements MessageListener {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private JedisPool jedisPool;

    private ConcurrentHashMap<String, Long> dataServers = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        MessageQueue messageQueue = new RedisMessageQueue("dataServer", jedisPool);
        messageQueue.start();
        messageQueue.consume(this);
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(2));
                    long now = System.currentTimeMillis();
                    List<String> removingServers = null;
                    for (Map.Entry<String, Long> entry : dataServers.entrySet()) {
                        //10s expired
                        if (entry.getValue() + 10000 < now) {
                            if (removingServers == null) {
                                removingServers = new ArrayList<>();
                            }
                            removingServers.add(entry.getKey());
                        }
                    }

                    if (removingServers != null) {
                        removingServers.forEach(item -> dataServers.remove(item));
                    }

                } catch (Throwable t) {

                    log.error("remove expired data server fail.", t);

                }
            }
        }, "remove-expired-dataserver-thread").start();


    }

    @Override
    public void onMessage(byte[] data) {
        dataServers.put(new String(data), System.currentTimeMillis());
    }

    public List<String> getAllActiveDataServers() {
        List<String> list = new ArrayList<>(dataServers.size());
        for (Map.Entry<String, Long> entry : dataServers.entrySet()) {
            list.add(entry.getKey());
        }
        return list;
    }

}
