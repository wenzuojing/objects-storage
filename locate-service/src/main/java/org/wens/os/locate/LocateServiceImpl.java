package org.wens.os.locate;


import com.alibaba.fastjson.JSONObject;
import org.wens.os.common.queue.MessageQueue;
import org.wens.os.common.queue.RedisMessageQueue;
import org.wens.os.common.util.UUIDS;
import redis.clients.jedis.JedisPool;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author wens
 */
public class LocateServiceImpl implements LocateService {

    private JedisPool jedisPool ;

    private MessageQueue messageQueue ;

    public LocateServiceImpl(String locateTopic , JedisPool jedisPool){
        this.jedisPool = jedisPool ;
        this.messageQueue = new RedisMessageQueue(locateTopic,jedisPool );
    }

    @Override
    public Map<String,String> locate(List<String> names,int expireSize) {

        assert expireSize >= 1 ;

        String topic  = UUIDS.uuid();
        MessageQueue tempMessageQueue = new RedisMessageQueue(topic ,jedisPool );
        tempMessageQueue.start();
        Map<String,String> results = new HashMap<>();
        CountDownLatch countDownLatch = new CountDownLatch(expireSize);
        tempMessageQueue.addMessageListener((m)-> {
            String result = new String(m);
            String[] items = result.split(",");
            if(!results.containsKey(items[0])){
                results.put(items[0],items[1]);
                countDownLatch.countDown();
            }

        } );
        // send locate message
        JSONObject message = new JSONObject();
        message.put("replyTo" , topic );
        message.put("names",names);
        messageQueue.send(message.toJSONString().getBytes(Charset.forName("utf-8")));
        try {
            countDownLatch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            //
        }
        messageQueue.close();
        return results;
    }
}
