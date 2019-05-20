package org.wens.os.matedata;


import com.alibaba.fastjson.JSONObject;

/**
 * @author wens
 */
public class MetaData {

    private String name ;

    private int version ;

    private String checksum ;

    private long size ;

    private JSONObject props ;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public JSONObject getProps() {
        return props;
    }

    public void setProps(JSONObject props) {
        this.props = props;
    }
}
