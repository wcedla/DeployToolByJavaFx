package fxft.util;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class InstallConfig {

    public static final String Key_installConfig = "installConfig";
    public static final String Key_httpDeployConfig = "httpDeployConfig";

    private Map<String, Map<String, String>> configMap = new LinkedHashMap<>();
    private Map<String, List<String>> moduleContainerMap = new LinkedHashMap<>();

    public String getInstallConfig(String key) {
        return getConfigValue(Key_installConfig, key);
    }

    public String getHttpDeployConfig(String key) {
        return getConfigValue(Key_httpDeployConfig, key);
    }

    public Set<String> getConfigKeys(String title) {
        Set<String> set = new LinkedHashSet<>();
        Map<String, String> map = configMap.get(title);
        if (map != null) {
            set.addAll(map.keySet());
        }
        return set;
    }

    public String getConfigValue(String title, String key) {
        Map<String, String> map = configMap.get(title);
        if (map != null) {
            return map.get(key);
        }
        return null;
    }

    public Set<String> getModuleContainerNames() {
        return moduleContainerMap.keySet();
    }

    public List<String> getModuleContainerValue(String name) {
        return moduleContainerMap.get(name);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InstallConfig{");
        sb.append("configMap=").append(configMap);
        sb.append(", moduleVersionMap=").append(moduleContainerMap);
        sb.append('}');
        return sb.toString();
    }

    public static InstallConfig readInstallConfigFile(Path filePath) throws Exception {
        InstallConfig config = new InstallConfig();
        config.parseInstallConfig(filePath);
        return config;
    }

    private void parseInstallConfig(Path configPath) throws Exception {
        try {
            Path cp = configPath;
            List<String> strings = Files.readAllLines(cp, Charset.forName("UTF-8"));
            String title = null;
            for (int i = 0; i < strings.size(); i++) {
                String line = strings.get(i).trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("[")) {
                    title = line.substring(1, line.length() - 1).trim();
                } else {
                    if (title == null) {
                        throw new Exception("第" + i + "行出错，找不到归属的类别！title=null; line=" + line);
                    }
                    if (title.equals(Key_installConfig) || title.equals(Key_httpDeployConfig)) {
                        readConfigLine(title, line, i);
                    } else {
                        readModuleVersionLine(title, line, i);
                    }
                }
            }

        } catch (Exception e) {
            throw new Exception("解析安装文件出错！path=" + configPath, e);
        }
    }

    private void readModuleVersionLine(String title, String line, int lineIndex) throws Exception {
        boolean success = false;
        String[] sarr = line.split(",");
        if(line.startsWith("-")){
            //vm参数配置
            success = true;
        }else if (sarr.length == 3) {
            if (sarr[1].trim().endsWith(".jar")) {
                List<String> mlist = moduleContainerMap.get(title);
                if (mlist == null) {
                    mlist = new ArrayList<>();
                    moduleContainerMap.put(title, mlist);
                }
                mlist.add(line.trim());
                success = true;
            }
        }
        if (!success) {
            throw new Exception("第" + lineIndex + "行解析失败，非moduleVersion.config的配置格式！title=" + title + "; line=" + line);
        }
    }

    private void readConfigLine(String title, String line, int lineIndex) throws Exception {
        boolean success = false;
        int i = line.indexOf("=");
        if (i > 0) {
            String key = line.substring(0, i).trim();
            String value = line.substring(i + 1).trim();
            if (key.length() > 0) {
                Map<String, String> cmap = configMap.get(title);
                if (cmap == null) {
                    cmap = new LinkedHashMap<>();
                    configMap.put(title, cmap);
                }
                cmap.put(key, value);
                success = true;
            }
        }
        if (!success) {
            throw new Exception("第" + lineIndex + "行解析失败！title=" + title + "; line=" + line);
        }
    }


}
