package org.wens.os.common.util;

import java.util.UUID;

public class UUIDS {

    public static String uuid(){
        return UUID.randomUUID().toString().replace("-","").toLowerCase();
    }
}
