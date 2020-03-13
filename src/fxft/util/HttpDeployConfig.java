package fxft.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author huanglusen
 * @date 2020-03-01
 */
public class HttpDeployConfig {

    private static Path httpdeployconfigPath = null;

    public static String[] getConfigValues(String deployURL, String profiles, String... keys) throws Exception {
        return getConfigValues(deployURL, profiles, Arrays.asList(keys), null);
    }

    public static String[] getConfigValues(String deployURL, String profiles, List<String> keys, List<String> setValues) throws Exception {
        String[] rearr = new String[keys.size()];
        Path userhome = Paths.get(System.getProperty("user.home"));
        Path cf = userhome.resolve(".httpdeployconfig.prperties");
        httpdeployconfigPath = cf.toAbsolutePath();
        List<String> lines = new ArrayList<>();
        if (Files.exists(cf)) {
            lines.addAll(Files.readAllLines(cf, StandardCharsets.UTF_8));
        }
        List<String> appendList = new ArrayList<>();
        for (int i=0 ; i<keys.size() ; i++) {
            String key = keys.get(i);
            String totalkey = null;
            if (profiles != null) {
                totalkey = String.format("[%s][%s].%s=", deployURL, profiles, key);
            } else {
                totalkey = String.format("[%s].%s=", deployURL, key);
            }
            for (String s : lines) {
                s = s.trim();
                if (s.startsWith(totalkey)) {
                    rearr[i] = s.substring(totalkey.length()).trim();
                }
            }
            if (rearr[i] == null) {
                String appendstr = totalkey;
                if (setValues != null) {
                    appendstr = totalkey + Optional.ofNullable(setValues.get(i)).orElse("");
                }
                appendList.add(appendstr);
            }
        }
        if (appendList.size() > 0) {
            Files.write(cf, appendList, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        return rearr;
    }

    public static String[] getNameAndPassword(String deployURL) throws Exception {
        String[] sarr = getConfigValues(deployURL, null, "loginName", "loginPassword");
        if (sarr[0] == null || sarr[1] == null || sarr[0].isEmpty() || sarr[1].isEmpty()) {
            throw new Exception("用户名或密码尚未配置！配置文件路径为：" + httpdeployconfigPath.toString());
        }
        return sarr;
    }

//    public static String[] getNameAndPassword(String deployURL) throws Exception {
//        String name = null;
//        String pwd = null;
//        String namekey = "[" + deployURL + "].loginName=";
//        String pwdKey = "[" + deployURL + "].loginPassword=";
//        Path userhome = Paths.get(System.getProperty("user.home"));
//        Path cf = userhome.resolve(".httpdeployconfig.prperties");
//        boolean append = false;
//        if (!Files.exists(cf)) {
//            append = true;
//        } else {
//            List<String> lines = Files.readAllLines(cf, Charset.forName("UTF-8"));
//            for (String s : lines) {
//                s = s.trim();
//                if (s.startsWith(namekey)) {
//                    name = s.substring(namekey.length()).trim();
//                }
//                if (s.startsWith(pwdKey)) {
//                    pwd = s.substring(pwdKey.length()).trim();
//                }
//            }
//        }
//        if (name == null || pwd == null) {
//            StringBuilder sb = new StringBuilder();
//            sb.append(namekey + "\n");
//            sb.append(pwdKey + "\n");
//            Files.write(cf, sb.toString().getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
//            throw new Exception("用户名或密码尚未配置！配置文件路径为：" + cf.toAbsolutePath().toString());
//        } else if (name.length() == 0 || pwd.length() == 0) {
//            throw new Exception("用户名或密码尚未配置！配置文件路径为：" + cf.toAbsolutePath().toString());
//        } else {
//            return new String[]{name, pwd};
//        }
//    }


}
