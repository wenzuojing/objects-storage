package org.wens.os.locate;

import java.util.List;

/**
 * @author wens
 */
public interface LocateService {

    List<String> locate(List<String> names,int expireSize ) ;


}
