package fxft.util;

import fxft.data.ModuleData;
import fxft.global.GlobalData;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InstallUtils {

    public static void installPackage(List<String> packageList, String installPath) {
        try {
            Class<InstallUtils> c = InstallUtils.class;
            Object o = c.newInstance();
            for (String name : packageList) {
                Method method = c.getDeclaredMethod("install" + name, String.class);
                if (method != null) {
                    method.invoke(o, installPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void installweb(String path) {
        try {
            FileUtils.copyDir(GlobalData.PACKAGE_RESOURCE_PATH + File.separator + "web", path);
            String batFilePath = path + File.separator + "web.bat";
            String cmd = "java -jar " + path + File.separator + "web-0.0.1-SNAPSHOT.jar ->" + System.getProperty("user.dir") + File.separator + "web.log";
            FileUtils.writeBatFile(batFilePath, cmd);
            ServerUtils.installService("", "web", batFilePath, "", "");
            System.out.println("安装web," + path);
            XmlUtils.writeXmlFile(new ModuleData("web", "web", path, ""));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void installmysql(String path) {

        System.out.println("安装mysql," + path);
    }

    private static void installkafka(String path) {
        System.out.println("安装kafka," + path);
    }

    private static void installredis(String path) {
        System.out.println("安装redis," + path);
    }

    public static void installOnlinePackageForWindows(Set<String> installSet, InstallConfig installConfig, Path installPath, Path tmpRunJarPath, String userName, String password) {
        for (String moduleName : installSet) {
            try {
                Path moduleInstallPath = installPath.resolve(moduleName).toAbsolutePath();
                if (Files.exists(moduleInstallPath)) {
                    System.out.println("安装的服务的文件夹已经创建过了" + moduleInstallPath);
                } else {
                    System.out.println("开始创建服务安装文件夹" + moduleInstallPath);
                    Files.createDirectories(moduleInstallPath);
                    Path logPath = moduleInstallPath.resolveSibling("logs").resolve(moduleName);
                    if (!Files.exists(logPath)) {
                        Files.createDirectories(logPath);
                        System.out.println("新建logs目录：" + logPath.toString());
                    }
                    Files.copy(tmpRunJarPath, moduleInstallPath.resolve("run.jar"));
                    List<CharSequence> moduleConfigDataList = new ArrayList<>();
                    moduleConfigDataList.add("run.deployServer=" + installConfig.getInstallConfig("deployServerURL"));
                    moduleConfigDataList.add("run.log.path=" + logPath.toAbsolutePath().toString());
                    StringBuilder vmArgs = new StringBuilder();
                    vmArgs.append(" -agentlib:libfxft-jardecoder");
                    for (String line : installConfig.getModuleContainerValue(moduleName)) {
                        if (line.startsWith("-")) {
                            vmArgs.append(" ");
                            vmArgs.append(line);
                        } else {
                            moduleConfigDataList.add(line);
                        }
                    }
                    if (vmArgs.indexOf("-Xmx") == -1) {
                        vmArgs.append(" -Xmx256M");
                    }
                    vmArgs.append(" -Drun.profiles=default");
                    Files.write(moduleInstallPath.resolve("moduleVersion.config"), moduleConfigDataList, StandardCharsets.UTF_8);
                    createWindowService(moduleName, moduleInstallPath, vmArgs.toString(), userName, password);
//                afterCreateContainer(containerName, containerDir, vmops);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void createWindowService(String moduleName, Path moduleInstallPath, String vmArgs, String userName, String password) {
        String batFilePath = moduleInstallPath.toAbsolutePath() + File.separator + moduleName + ".bat";
        System.out.println("bat文件地址:" + batFilePath);
        String cmd = "cd /d " + moduleInstallPath.toAbsolutePath() + "\njava -jar" + vmArgs + " run.jar" + " > " + moduleInstallPath.toAbsolutePath() + File.separator + moduleName + ".log";
        System.out.println("bat命令:" + cmd);
        FileUtils.writeBatFile(batFilePath, cmd);
        String serviceBatPath = moduleInstallPath.toAbsolutePath() + File.separator + moduleName + "Service.bat";
        System.out.println("开始安装服务");
        ServerUtils.installService(serviceBatPath, moduleName, batFilePath, userName, password);
//        System.out.println("安装web," + path);
        System.out.println("开始写入xml文件");
        XmlUtils.writeXmlFile(new ModuleData(moduleName, moduleName, moduleInstallPath.toString(), moduleInstallPath + File.separator + moduleName + ".log"));
    }

    public static void installOnlinePackageForLinux(Set<String> installSet, InstallConfig installConfig, Path installPath, Path tmpRunJarPath, String userName, String password) {
        for (String moduleName : installSet) {
            try {
                Path moduleInstallPath = installPath.resolve(moduleName).toAbsolutePath();
                if (Files.exists(moduleInstallPath)) {
                    System.out.println("安装的服务的文件夹已经创建过了" + moduleInstallPath);
                } else {
                    System.out.println("开始创建服务安装文件夹" + moduleInstallPath);
                    Files.createDirectories(moduleInstallPath);
                    Path logPath = moduleInstallPath.resolveSibling("logs").resolve(moduleName);
                    if (!Files.exists(logPath)) {
                        Files.createDirectories(logPath);
                        System.out.println("新建logs目录：" + logPath.toString());
                    }
                    Files.copy(tmpRunJarPath, moduleInstallPath.resolve("run.jar"));
                    List<CharSequence> moduleConfigDataList = new ArrayList<>();
                    moduleConfigDataList.add("run.deployServer=" + installConfig.getInstallConfig("deployServerURL"));
                    moduleConfigDataList.add("run.log.path=" + logPath.toAbsolutePath().toString());
                    StringBuilder vmArgs = new StringBuilder();
                    vmArgs.append(" -agentlib:libfxft-jardecoder");
                    for (String line : installConfig.getModuleContainerValue(moduleName)) {
                        if (line.startsWith("-")) {
                            vmArgs.append(" ");
                            vmArgs.append(line);
                        } else {
                            moduleConfigDataList.add(line);
                        }
                    }
                    if (vmArgs.indexOf("-Xmx") == -1) {
                        vmArgs.append(" -Xmx256M");
                    }
                    vmArgs.append(" -Drun.profiles=default");
                    Files.write(moduleInstallPath.resolve("moduleVersion.config"), moduleConfigDataList, StandardCharsets.UTF_8);
                    createLinuxService(moduleName, moduleInstallPath.toAbsolutePath().toString(), vmArgs.toString());
//                afterCreateContainer(containerName, containerDir, vmops);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void createLinuxService(String moduleName, String moduleInstallPath, String vmArgs) {
        String systemPath = "/usr/lib/systemd/system";
        String javaPath = System.getProperty("user.dir") + File.separator + "jre" + File.separator + "bin" + File.separator + "java";
        String jarPath = moduleInstallPath + File.separator + "run.jar";
        String serviceContent = buildServiceContent(moduleName, javaPath, jarPath, vmArgs);
        System.out.println("获取service内容:" + serviceContent);
        File serviceFile = new File(systemPath + moduleName + ".service");
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(serviceFile);
            fileOutputStream.write(serviceContent.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("写入service文件完成");
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("开始写入xml文件");
        XmlUtils.writeXmlFile(new ModuleData(moduleName, moduleName, moduleInstallPath.toString(), moduleInstallPath + File.separator + moduleName + ".log"));
    }

    private static String buildServiceContent(String moduleName, String javaPath, String jarPath, String vmArgs) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[Unit]\nDescription=");
        stringBuilder.append(moduleName);
        stringBuilder.append("\nAfter=syslog.target network.target");
        stringBuilder.append("\n[Service]\nType=simple");
        stringBuilder.append("\nExecStart=");
        stringBuilder.append(javaPath);
        stringBuilder.append(" -jar ");
        stringBuilder.append(vmArgs);
        stringBuilder.append(" ");
        stringBuilder.append(jarPath);
        stringBuilder.append("\nExecStop=/bin/kill -15 $MAINPID");
        stringBuilder.append("\nUser=root\nGroup=root\n[Install]\nWantedBy=multi-user.target");
        return stringBuilder.toString();
    }

}
