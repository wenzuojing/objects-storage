package org.wens.os.locate;


import com.alibaba.fastjson.JSONObject;
import org.wens.os.common.jgroups.JGroupsMessageQueue;
import org.wens.os.common.jgroups.MessageListener;
import org.wens.os.common.util.UUIDS;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author wens
 */
public class LocateServiceImpl implements LocateService {

    private JGroupsMessageQueue jGroupsMessageQueue;


    public LocateServiceImpl(String locateTopic) {
        this.jGroupsMessageQueue = new JGroupsMessageQueue(locateTopic);
    }

    @Override
    public Map<String, String> locate(List<String> names, int expireSize) {

        assert expireSize >= 1;

        String tag = UUIDS.uuid();
        Map<String, String> results = new ConcurrentHashMap<>();
        CountDownLatch countDownLatch = new CountDownLatch(expireSize);
        MessageListener messageListener = (context) -> {
            if (!tag.equals(context.getTag())) {
                return;
            }
            String result = new String(context.getMessage());
            String[] items = result.split(",");
            if (!results.containsKey(items[0])) {
                results.put(items[0], items[1]);
                countDownLatch.countDown();
            }

        };

        jGroupsMessageQueue.addMessageListener(messageListener);
        // send locate message
        jGroupsMessageQueue.send(JSONObject.toJSONBytes(names), tag);
        try {
            countDownLatch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            //
        }
        return results;
    }
}
