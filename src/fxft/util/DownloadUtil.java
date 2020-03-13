package fxft.util;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

public class DownloadUtil {

    private static String[] calcMd5(LinkedHashMap<String, String> map, String deployUrl) throws Exception {
        StringBuffer sb = new StringBuffer();
        for (String v : map.values()) {
            sb.append(v);
        }
        String[] nameAndPassword = HttpDeployConfig.getNameAndPassword(deployUrl);
        String loginName = nameAndPassword[0];
        String loginPwd = nameAndPassword[1];
        sb.append(loginPwd);
        String md5str = sb.toString();
        return new String[]{loginName, Md5Util.md5(md5str)};
    }

    public static Path downloadFile(String fileName, String parent, boolean reqNonEncJar, String deployUrl, Path targetDir) throws Exception {
        try {
            LinkedHashMap<String, String> params = new LinkedHashMap<>();
            params.put("fileName", fileName);
            params.put("parent", parent);
            params.put("reqNonEncJar", String.valueOf(reqNonEncJar));
            params.put("time", String.valueOf(System.currentTimeMillis()));
            String[] sign = calcMd5(params, deployUrl);
            params.put("loginName", sign[0]);
            params.put("sign", sign[1]);
            String requrl = deployUrl + "/deploy/install/getFile";
            byte[] fbytes = HttpUtil.postRequestBytes(requrl, params, "UTF-8", 1000, 60000);
            if (fbytes.length == 0) {
                throw new Exception("下载部署文件为空！fileName=" + fileName);
            }
            Path repath = targetDir.resolve(fileName);
            Files.write(repath, fbytes);
            System.out.println("下载安装文件！target=" + repath.toAbsolutePath().toString() + "; fbytes=" + fbytes.length);
            return repath;
        } catch (Exception e) {
            throw new Exception("下载文件出错！fileName=" + fileName + "; parent=" + parent, e);
        }
    }


}
