package fxft.data;

public class NavigationTreeItemData {

    private int type;

    private String name;

    private String server;

    private String path;

    private String serverLogPath;

    private String debugLogPath;

    public NavigationTreeItemData(int type, String name, String server, String path, String serverLogPath, String debugLogPath) {
        this.type = type;
        this.name = name;
        this.server = server;
        this.path = path;
        this.serverLogPath = serverLogPath;
        this.debugLogPath = debugLogPath;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getServerLogPath() {
        return serverLogPath;
    }

    public void setServerLogPath(String serverLogPath) {
        this.serverLogPath = serverLogPath;
    }

    public String getDebugLogPath() {
        return debugLogPath;
    }

    public void setDebugLogPath(String debugLogPath) {
        this.debugLogPath = debugLogPath;
    }
}
