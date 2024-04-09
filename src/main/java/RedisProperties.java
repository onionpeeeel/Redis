public class RedisProperties {

    private boolean isReplicaOff;
    private String masterNode;
    private String masterPort;
    private String replicationId = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
    private Integer replicationOffset = 0;

    private String RDBContent = "UkVESVMwMDEx+glyZWRpcy12ZXIFNy4yLjD6CnJlZGlzLWJpdHPAQPoFY3RpbWXCbQi8ZfoIdXNlZC1tZW3CsMQQAPoIYW9mLWJhc2XAAP/wbjv+wP9aog==";

    public String getMasterNode() {
        return masterNode;
    }

    public void setMasterNode(String masterNode) {
        this.masterNode = masterNode;
    }

    public String getMasterPort() {
        return masterPort;
    }

    public void setMasterPort(String masterPort) {
        this.masterPort = masterPort;
    }

    public String getReplicationId() {
        return replicationId;
    }

    public void setReplicationId(String replicationId) {
        this.replicationId = replicationId;
    }

    public Integer getReplicationOffset() {
        return replicationOffset;
    }

    public void setReplicationOffset(Integer replicationOffset) {
        this.replicationOffset = replicationOffset;
    }

    public String getRDBContent() {
        return RDBContent;
    }

    public void setRDBContent(String rdbContent) {
        this.RDBContent = rdbContent;
    }

    public boolean isReplicaOff() {
        return isReplicaOff;
    }

    public void setReplicaOff(boolean replicaOff) {
        isReplicaOff = replicaOff;
    }
}
