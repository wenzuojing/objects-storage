package org.wens.os.dataserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.wens.os.common.jgroups.JGroupsMessageQueue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wens
 */
@Service
public class HeartbeatService implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(this.getClass());


    @Value("${server.address}:${server.port}")
    private String listenAddress;


    @Override
    public void run(String... args) throws Exception {
        JGroupsMessageQueue groupsMessageQueue = new JGroupsMessageQueue("dataServer");
        new Thread(() -> {
            AtomicBoolean stopped = new AtomicBoolean(false);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> stopped.set(true)));

            while (!stopped.get()) {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(2));
                    groupsMessageQueue.send(listenAddress.getBytes(), null);
                } catch (Throwable t) {
                    log.error("send heartbeat fail", t);
                }
            }

        }, "heartbeat-thread").start();


    }
}
