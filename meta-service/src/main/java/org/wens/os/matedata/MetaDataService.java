package org.wens.os.matedata;

import java.util.List;

/**
 * @author wens
 */
public interface MetaDataService {


    MetaData findLastVersion(String name );

    MetaData find(String name , int version );

    void delete(String name );

    void save(MetaData metaData);

    List<MetaData> findAllVersion(String name);

}
