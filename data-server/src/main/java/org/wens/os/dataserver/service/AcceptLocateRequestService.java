package org.wens.os.dataserver.service;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.wens.os.common.queue.MessageQueue;
import org.wens.os.common.queue.RedisMessageQueue;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wens
 */
@Service
public class AcceptLocateRequestService implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private JedisPool jedisPool;

    @Resource
    private StorageInstanceService storageInstanceService ;

    @Value("${server.address}:${server.port}")
    private String listenAddress;

    private ConcurrentHashMap<String,Long> nameMap = new ConcurrentHashMap<>() ;

    @Override
    public void run(String... args) throws Exception {

        new Thread(()->{
            try{
                List<StorageService.Key> keys = storageInstanceService.getObjectsStorageService().list(key -> !key.key.endsWith(".checksum"));
                Long l = System.currentTimeMillis();
                keys.forEach( k -> nameMap.put(k.key,l));
            }catch (Throwable t){
                log.error("load key fail.",t );
                Runtime.getRuntime().exit(-1);
            }


            MessageQueue messageQueue = new RedisMessageQueue("locateMessage" , jedisPool );
            messageQueue.start();

            messageQueue.addMessageListener(m -> {
                JSONObject jsonObject = JSONObject.parseObject(new String(m, Charset.forName("utf-8")));
                MessageQueue tempMessageQueue = null ;
                for(String name : jsonObject.getJSONArray("names").toJavaList(String.class) ){
                    if(nameMap.containsKey(name)){
                        if(tempMessageQueue == null ){
                            tempMessageQueue = new RedisMessageQueue(jsonObject.getString("replyTo"), jedisPool) ;
                        }
                        tempMessageQueue.send( (name + ","+ listenAddress).getBytes(Charset.forName("utf-8")));
                    }
                }
                if( tempMessageQueue != null ){
                    tempMessageQueue.close();
                }
            });


        },"accept-locate-request-thread").start();

    }

    public void addName(String name ){
        nameMap.put(name,System.currentTimeMillis());
    }

    public void removeName(String name){
        nameMap.remove(name);
    }
}
