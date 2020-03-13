package fxft.global;

import java.io.File;

public class GlobalData {

    public final static String FILE_INIT_SHOW_PATH=System.getProperty("user.home")+ File.separator+"Desktop";

    public final static String INSTALLED_XML_FILE_PATH =System.getProperty("user.home")+ File.separator+"fxft_deploy.xml";

    public final static String PACKAGE_RESOURCE_PATH=System.getProperty("user.dir")+ File.separator+"package";

    public final static int INSTALLED_ROOT_TYPE =0;

    public final static int INSTALLED_TYPE =1;

    public final static int NOT_INSTALL_ROOT_TYPE =2;

    public final static int NOT_INSTALL_TYPE =3;

    public final static String CONFIG_FILE_NAME="ascs.install";
    public final static String CONFIG_SERVER_URL="http://172.16.8.160:3002/ascs";

    public final static String OS_NAME = System.getProperty("os.name").toLowerCase();

}
