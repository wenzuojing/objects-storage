package org.wens.os.apiserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wens.os.common.OSException;
import org.wens.os.common.jgroups.JGroupsMessageQueue;
import org.wens.os.common.jgroups.MessageContext;
import org.wens.os.common.jgroups.MessageListener;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author wens
 */
@Service
public class ActiveDataServerService implements MessageListener {

    private Logger log = LoggerFactory.getLogger(this.getClass());


    private ConcurrentHashMap<String, Long> dataServers = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        JGroupsMessageQueue groupsMessageQueue = new JGroupsMessageQueue("dataServer");
        groupsMessageQueue.addMessageListener(this);
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
    public void onMessage(MessageContext context) {
        dataServers.put(new String(context.getMessage()), System.currentTimeMillis());
    }

    public List<String> getAllActiveDataServers() {
        List<String> list = new ArrayList<>(dataServers.size());
        for (Map.Entry<String, Long> entry : dataServers.entrySet()) {
            list.add(entry.getKey());
        }
        return list;
    }

    public List<String> random(int i) {
        List<String> servers = getAllActiveDataServers();
        if (servers.size() < i) {
            throw new OSException("There is not enough server");
        }
        Random random = new Random(System.currentTimeMillis());
        Set<Integer> indexS = new HashSet<>();
        while (indexS.size() != i) {
            indexS.add(random.nextInt(servers.size()));
        }
        List<String> ret = new ArrayList<>(i);
        indexS.forEach(index -> ret.add(servers.get(index)));
        return ret;
    }

}
