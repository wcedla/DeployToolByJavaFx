package fxft.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

public class HttpUtil {


    private HttpUtil() {
    }

    /**
     * 发送http请求
     *
     * @param urlstr    String  目标URL地址
     * @param sessionid String  在目标server的sessionid
     * @return String        HttpResponse
     * @throws Exception
     */
    public static String getRequest(String urlstr, String sessionid) throws IOException {
        int i = urlstr.indexOf("?");
        if (i != -1) {
            urlstr = urlstr.substring(0, i) + ";jsessionid=" + sessionid + urlstr.substring(i);
        } else {
            urlstr = urlstr + ";jsessionid=" + sessionid;
        }

        URL url = new URL(urlstr);
        try {
            HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
            InputStream is = urlconn.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
            urlconn.disconnect();
            return sb.toString();
        } catch (IOException ex) {
            System.out.println("建立http连接失败！url:" + urlstr);
            throw ex;
        }
    }


    public static String getRequestWithEncode(String urlstr, String encode) throws IOException {
        URL url = new URL(urlstr);
        try {
            HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
            InputStream is = urlconn.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(is, encode));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
            urlconn.disconnect();
            return sb.toString();
        } catch (Exception ex) {
            System.out.println("建立http连接失败！url:" + urlstr);
            throw new IOException(ex);
        }
    }

    public static String postRequestString(String urlstr, Map<String, String> params, String encode, int connTimeout, int readTimeout)
            throws Exception {
        byte[] barr = postRequestBytes(urlstr, params, encode, connTimeout, readTimeout);
        return new String(barr, encode);
    }


    /**
     * 提交Post请求
     *
     * @param urlstr
     * @param params  post的参数
     * @param encode  编码，UTF-8/GBK
     * @return
     * @throws Exception
     */
    public static byte[] postRequestBytes(String urlstr, Map<String, String> params, String encode, int connTimeout, int readTimeout)
            throws Exception {
        try {
            URL url = new URL(urlstr);
            HttpURLConnection post = (HttpURLConnection) url.openConnection();
            if (connTimeout > 0) {
                post.setConnectTimeout(connTimeout);
            }
            if (readTimeout > 0) {
                post.setReadTimeout(readTimeout);
            }
            post.setRequestMethod("POST");
            post.setDoInput(true);
            post.setDoOutput(true);
            post.connect();
            OutputStream os = post.getOutputStream();
            StringBuffer sb = new StringBuffer();
            if (params != null) {
                Iterator<String> keyiter = params.keySet().iterator();
                while (keyiter.hasNext()) {
                    String key = keyiter.next();
                    String value = params.get(key);
                    if (sb.length() != 0) {
                        sb.append("&");
                    }
                    sb.append(key + "=" + URLEncoder.encode(value, encode));
                }
            }
            os.write(sb.toString().getBytes(encode));
            os.flush();
            os.close();
            int respcode = post.getResponseCode();
            InputStream is = post.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] barr = new byte[1024];
            int i = is.read(barr);
            while (i != -1) {
                bos.write(barr, 0, i);
                i = is.read(barr);
            }
            post.disconnect();
            if (respcode != 200) {
                throw new Exception("返回Http状态码不正确！respcode=" + respcode);
            } else {
                return bos.toByteArray();
            }
        } catch (Exception e) {
            throw e;
        }
    }


}
