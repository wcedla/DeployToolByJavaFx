package fxft.data;

public class ModuleTreeTableItemData {

    private String name;

    private String version;

    private String status;

    private String server;

    private String logPath;

    public ModuleTreeTableItemData(String name, String version, String status, String server, String logPath) {
        this.name = name;
        this.version = version;
        this.status = status;
        this.server = server;
        this.logPath = logPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }
}
