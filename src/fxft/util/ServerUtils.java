package fxft.util;

import java.io.*;

import static fxft.global.GlobalData.OS_NAME;

public class ServerUtils {

    public static boolean isServerRunning(String serverName) {
        Process process = null;
        Runtime runtime = Runtime.getRuntime();
        try {
            process = runtime.exec(System.getProperty("user.dir") + File.separator + "nssm.exe" + " status " + serverName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String status = bufferedReader.readLine();
            if (status != null) {
                status = status.replaceAll("\0", "");
                if (status.equals("SERVICE_RUNNING")) {
                    process.destroy();
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    public static void startService(String serverName) {
        Process process = null;
        Runtime runtime = Runtime.getRuntime();
        try {
            if (OS_NAME.contains("win")) {
                process = runtime.exec(System.getProperty("user.dir") + File.separator + "nircmd.exe elevate cmd /k " + System.getProperty("user.dir") + File.separator + "nssm.exe start " + serverName);
            } else {
                process = runtime.exec("systemctl start " + serverName + ".service");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopService(String serverName) {
        Process process = null;
        Runtime runtime = Runtime.getRuntime();
        try {
            if (OS_NAME.contains("win")) {
                process = runtime.exec(System.getProperty("user.dir") + File.separator + "nircmd.exe elevate cmd /k " + System.getProperty("user.dir") + File.separator + "nssm.exe stop " + serverName);
            } else {
                process = runtime.exec("systemctl stop " + serverName + ".service");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void restartService(String serverName) {
        Process process = null;
        Runtime runtime = Runtime.getRuntime();
        try {
            if (OS_NAME.contains("win")) {
                process = runtime.exec(System.getProperty("user.dir") + File.separator + "nircmd.exe elevate cmd /k " + System.getProperty("user.dir") + File.separator + "nssm.exe restart " + serverName);
            } else {
                process = runtime.exec("systemctl restart " + serverName + ".service");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void installService(String batPath, String serverName, String batFilePath, String userName, String password) {
        Process process = null;
        Runtime runtime = Runtime.getRuntime();
        try {
            String installCmd = System.getProperty("user.dir") + File.separator + "nircmd.exe elevate cmd /k " + System.getProperty("user.dir") + File.separator + "nssm.exe install " + serverName + " " + batFilePath;
            String editCmd = System.getProperty("user.dir") + File.separator + "nircmd.exe elevate cmd /k " + System.getProperty("user.dir") + File.separator + "nssm.exe set " + serverName + " ObjectName .\\" + userName + " " + password;
            String waitCmd = "TIMEOUT /T 5";
            String startCmd = System.getProperty("user.dir") + File.separator + "nircmd.exe elevate cmd /k " + System.getProperty("user.dir") + File.separator + "nssm.exe start " + serverName;
            String cmd = installCmd + "\n" + waitCmd + "\n" + editCmd + "\n" + waitCmd + "\n" + startCmd;
            FileUtils.writeBatFile(batPath, cmd);
            process = runtime.exec("cmd /k " + batPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
