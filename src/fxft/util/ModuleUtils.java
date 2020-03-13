package fxft.util;

import fxft.data.ModuleInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleUtils {

    private volatile static ModuleUtils moduleUtils;
    private List<ModuleInfo> moduleInfo = new ArrayList<>();

//    public static ModuleUtils getInstance(File rootDirectory,String[] serverName) {
//        if (moduleUtils == null) {
//            synchronized (ModuleUtils.class) {
//                if (moduleUtils == null) {
//                    moduleUtils = new ModuleUtils();
//                }
//            }
//        }
//        moduleUtils.moduleInfo=findModuleFile(rootDirectory,serverName);
//        return moduleUtils;
//    }

    public static List<ModuleInfo> findModuleFile(File rootDirectory, String serverName) {
        List<ModuleInfo> moduleList = new ArrayList<>();
        if (rootDirectory != null) {
            if (rootDirectory.isDirectory()) {
                File[] files = rootDirectory.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (file.getName().equals("moduleVersion.config")) {
                            try {
                                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                                String content;
                                while ((content = bufferedReader.readLine()) != null) {
                                    if (content.contains(".jar") && content.charAt(0) != '#') {
                                        String[] moduleInfo = content.split(",");
                                        if (moduleInfo.length == 3) {
                                            moduleList.add(new ModuleInfo(moduleInfo[0].trim(), moduleInfo[2].trim(), "serverStatusWrapper(isServiceRunning)", serverName));
                                        }
                                    }
                                }
                                return moduleList;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    private static String serverStatusWrapper(boolean isRunning) {
        return (isRunning ? "运行" : "停止");
    }

    public List<ModuleInfo> getModuleInfoList() {
        return moduleInfo;
    }

    public static boolean isModuleDirectory(String path) {
        File moduleFile = new File(path);
        if (moduleFile.exists()) {
            File[] files = moduleFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().equals("moduleVersion.config")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
