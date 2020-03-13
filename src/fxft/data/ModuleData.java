package fxft.data;

public class ModuleData {

    private String moduleName;

    private String serverName;

    private String modulePath;

    private String serverLogPath;

    public ModuleData(String moduleName, String serverName, String modulePath, String serverLogPath) {
        this.moduleName = moduleName;
        this.serverName = serverName;
        this.modulePath = modulePath;
        this.serverLogPath = serverLogPath;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getModulePath() {
        return modulePath;
    }

    public void setModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    public String getServerLogPath() {
        return serverLogPath;
    }

    public void setServerLogPath(String serverLogPath) {
        this.serverLogPath = serverLogPath;
    }
}
