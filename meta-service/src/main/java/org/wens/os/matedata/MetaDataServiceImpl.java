package org.wens.os.matedata;

import com.alibaba.fastjson.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.wens.os.common.util.StringUtils;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author wens
 */
@Service
public class MetaDataServiceImpl implements MetaDataService {

    private static class MetaDataRowMapper implements RowMapper<MetaData>{

        @Override
        public MetaData mapRow(ResultSet resultSet, int i) throws SQLException {
            MetaData metaData = new MetaData() ;
            metaData.setName(resultSet.getString("name"));
            metaData.setVersion(resultSet.getInt("version"));
            metaData.setChecksum(resultSet.getString("checksum"));
            metaData.setSize(resultSet.getInt("size"));
            metaData.setProps(JSONObject.parseObject(resultSet.getString("props")));
            return metaData;
        }
    }

    @Resource
    private JdbcTemplate jdbcTemplate ;

    @Override
    public MetaData findLastVersion(String name) {
        String sql = "select * from meta_data where name = ? order by version desc limit 1";
        return queryOne( sql,name);
    }

    @Override
    public MetaData find(String name, int version) {
        String sql = "select * from meta_data where name = ? and version = ? ";
        return queryOne(sql,name,version);
    }

    @Override
    public void delete(String name) {

        MetaData metaData = findLastVersion(name);
        if( metaData == null ){
            return;
        }
        metaData.setProps(new JSONObject());
        metaData.setSize(0);
        metaData.setChecksum(StringUtils.EMPTY);
        metaData.setVersion(metaData.getVersion() + 1 );
        save(metaData);
    }

    @Override
    public void save(MetaData metaData) {
        String sql = "insert into meta_data (name , version , checksum , size , props ) values (?,?,?,?,?)" ;
        jdbcTemplate.update(sql, metaData.getName(),metaData.getVersion(),metaData.getChecksum(),metaData.getSize(),metaData.getProps().toJSONString() );
    }

    @Override
    public List<MetaData> findAllVersion(String name) {
        String sql = "select * from meta_data where name = ? order by version ";
        return jdbcTemplate.query(sql,new MetaDataRowMapper(),name);
    }

    private MetaData queryOne(String sql ,Object... args) {
        List<MetaData> list = jdbcTemplate.query(sql, new MetaDataRowMapper(), args);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
}
