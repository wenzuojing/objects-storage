package org.wens.os.locate;

import java.util.List;
import java.util.Map;

/**
 * @author wens
 */
public interface LocateService {

    Map<String,String> locate(List<String> names, int expireSize ) ;


}
