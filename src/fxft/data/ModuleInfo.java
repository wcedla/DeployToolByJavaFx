package fxft.data;

public class ModuleInfo {

    String moduleName;

    String moduleVersion;

    String moduleStatus;

    String moduleServerName;


    public ModuleInfo(String moduleName, String moduleVersion, String moduleStatus, String moduleServerName) {
        this.moduleName = moduleName;
        this.moduleVersion = moduleVersion;
        this.moduleStatus = moduleStatus;
        this.moduleServerName = moduleServerName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public String getModuleStatus() {
        return moduleStatus;
    }

    public void setModuleStatus(String moduleStatus) {
        this.moduleStatus = moduleStatus;
    }

    public String getModuleServerName() {
        return moduleServerName;
    }

    public void setModuleServerName(String moduleServerName) {
        this.moduleServerName = moduleServerName;
    }
}
