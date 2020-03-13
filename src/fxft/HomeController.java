package fxft;

import fxft.data.*;
import fxft.util.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import fxft.custom.CustomListViewCell;
import fxft.custom.CustomTreeCell;
import fxft.global.GlobalData;
import org.apache.log4j.helpers.FileWatchdog;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

import static fxft.global.GlobalData.*;

public class HomeController implements Initializable {

    @FXML
    public ProgressIndicator progressbar;

    @FXML
    public BorderPane loadingRoot;

    @FXML
    public AnchorPane rootPane;

    @FXML
    public TreeView<NavigationTreeItemData> navigationTreeView;

    @FXML
    public TextArea moduleLogTextArea;

    @FXML
    public AnchorPane installedRootPane;

    @FXML
    public TextArea moduleConfigEdit;

    @FXML
    public AnchorPane moduleConfigEditRoot;

    @FXML
    public Button saveEdit;

    @FXML
    public Button reloadEdit;

    @FXML
    public ComboBox<String> moduleLogSelect;

    @FXML
    public AnchorPane moduleLogRoot;

    @FXML
    public Button startService;

    @FXML
    public AnchorPane notInstallRootPane;

    @FXML
    public AnchorPane serverStartLogRoot;

    @FXML
    public TextArea deployTextArea;

    @FXML
    public ListView<ListViewCellData> installListView;

    @FXML
    public Button installConfirm;

    @FXML
    public Button stopService;

    @FXML
    public TreeTableView<ModuleTreeTableItemData> moduleTreeTable;

    @FXML
    public TreeTableColumn<ModuleTreeTableItemData, String> nameColumn;

    @FXML
    public TreeTableColumn<ModuleTreeTableItemData, String> versionColumn;

    @FXML
    public TreeTableColumn<ModuleTreeTableItemData, String> statusColumn;

    @FXML
    public Button restartService;

    @FXML
    public Button refreshService;

    @FXML
    public Button installPath;

    @FXML
    public Label installPathText;

    @FXML
    public Button installOk;
    @FXML
    public Label windowsUserName;
    @FXML
    public TextField windowsPwd;

    ObservableList<ListViewCellData> notInstallList = FXCollections.observableArrayList();

    private ThreadPoolExecutor initThreadPool = new ThreadPoolExecutor(2, 5, 1, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(10), r -> {
        Thread thread = new Thread(r);
        thread.setName("InitThread");
        return thread;
    }, new ThreadPoolExecutor.DiscardOldestPolicy());

    /**
     * 初始化
     *
     * @param location  fxml文件url地址
     * @param resources 资源文件地址
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getConfigData();
        initData();
        initListener();
    }

    private InstallConfig installConfig;
    Path tmpRunJarPath;
    private volatile List<ModuleData> installedModuleList = new ArrayList<>();
    Future<?> future = null;
    Set<String> notInstallSet = new HashSet<>();
    private String tmpModulePath = "";
    private FileWatchdog moduleLogWatchDog = null;
    String tmpDeployLogPath = "";
    FileWatchdog deployLogFileWatchdog = null;
    private volatile boolean stopDeployLogWatch = false;

    /**
     * 获取服务安装的配置信息
     */
    private void getConfigData() {
        try {
            Path tmpDir = Paths.get("tmp");
            if (!Files.exists(tmpDir)) {
                Files.createDirectories(tmpDir);
            }
            Path configPath = Paths.get("config");
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath);
            }
            Path installConfigPath;
            if (Files.exists(configPath.resolve(CONFIG_FILE_NAME))) {
                installConfigPath = configPath.resolve(CONFIG_FILE_NAME);
            } else {
                installConfigPath = DownloadUtil.downloadFile(CONFIG_FILE_NAME, "installConfig", false, CONFIG_SERVER_URL, configPath);
            }
            installConfig = InstallConfig.readInstallConfigFile(installConfigPath);
            System.out.println("读取安装配置文件:" + installConfig);
            String deployUrl = installConfig.getInstallConfig("deployServerURL");
//            String profiles = installConfig.getInstallConfig("profiles");
            String installDir = installConfig.getInstallConfig("installDir");
            if (Files.exists(tmpDir.resolve("run.jar"))) {
                tmpRunJarPath = tmpDir.resolve("run.jar");
            } else {
                tmpRunJarPath = DownloadUtil.downloadFile("run.jar", "libs", false, deployUrl, tmpDir);
            }
            installPathText.setText(Paths.get(installDir).toAbsolutePath().toString());
            initNavigationInstalledAndNotInstallData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理导航树的已经安装和未安装的数据
     */
    private void initNavigationInstalledAndNotInstallData() {
        TreeItem<NavigationTreeItemData> navigationTreeRoot = new TreeItem<>(new NavigationTreeItemData(-1, "", "", "", "", ""));
        TreeItem<NavigationTreeItemData> installedRoot = new TreeItem<>(new NavigationTreeItemData(GlobalData.INSTALLED_ROOT_TYPE, "已部署服务", "", "installedRoot", "", ""));
        TreeItem<NavigationTreeItemData> notInstallRoot = new TreeItem<>(new NavigationTreeItemData(GlobalData.NOT_INSTALL_TYPE, "待安装服务", "", "notInstallRoot", "", ""));
        installedRoot.setExpanded(true);
        notInstallRoot.setExpanded(true);
        File xmlConfigurationFile = new File(GlobalData.INSTALLED_XML_FILE_PATH);
        System.out.println("部署配置文件路径:" + xmlConfigurationFile.getPath() + ",是否存在:" + xmlConfigurationFile.exists());
        if (xmlConfigurationFile.exists()) {
            getInstalledData(installedRoot);
            if (!installedModuleList.isEmpty()) {
                initModuleTreeTableData();
            }
        }
        initNotInstallData(notInstallRoot);
        navigationTreeRoot.getChildren().add(installedRoot);
        navigationTreeRoot.getChildren().add(notInstallRoot);
        navigationTreeView.setRoot(navigationTreeRoot);
        navigationTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        navigationTreeView.setShowRoot(false);
        //navigationTreeView.getSelectionModel().selectFirst();
        loadingRoot.setVisible(false);
    }

    /**
     * 添加已经安装的服务到导航树的已经安装的服务的root节点
     *
     * @param installedRoot 导航树的已经安装的服务的root节点
     */
    private void getInstalledData(TreeItem<NavigationTreeItemData> installedRoot) {
        installedModuleList = XmlUtils.parseXmlFile();
        System.out.println("解析xml文件，当前已经安装的服务个数:" + installedModuleList.size());
        if (!installedModuleList.isEmpty()) {
            for (ModuleData moduleData : installedModuleList) {
                TreeItem<NavigationTreeItemData> installedItem = new TreeItem<>(new NavigationTreeItemData(GlobalData.INSTALLED_TYPE, moduleData.getModuleName(), moduleData.getServerName(), moduleData.getModulePath(), moduleData.getServerLogPath(), moduleData.getModulePath().substring(0, moduleData.getModulePath().lastIndexOf("\\")) + File.separator + "logs" + File.separator + moduleData.getModuleName()));
                installedRoot.getChildren().add(installedItem);
            }
        }
    }

    /**
     * 初始化已经安装的模块信息树表格
     */
    private void initModuleTreeTableData() {
        future = initThreadPool.submit(() -> {
            if (!installedModuleList.isEmpty()) {
                TreeItem<ModuleTreeTableItemData> rootNode = new TreeItem<>(new ModuleTreeTableItemData("root", "", "", "", ""));
                for (ModuleData moduleData : installedModuleList) {
                    ObservableList<ModuleTreeTableItemData> ModuleTreeTableItemDataList = FXCollections.observableArrayList();
                    List<ModuleInfo> moduleInfoList = ModuleUtils.findModuleFile(new File(moduleData.getModulePath()), moduleData.getServerName());
                    String serverStatus = serverStatusWrapper(ServerUtils.isServerRunning(moduleData.getServerName()));
                    System.out.println("检测服务:" + moduleData.getServerName() + "状态为:" + serverStatus);
                    for (ModuleInfo moduleInfo : moduleInfoList) {
                        ModuleTreeTableItemDataList.add(new ModuleTreeTableItemData(moduleInfo.getModuleName(), moduleInfo.getModuleVersion(), "", moduleInfo.getModuleServerName(), moduleData.getServerLogPath()));
                    }
                    TreeItem<ModuleTreeTableItemData> moduleRoot = new TreeItem<>(new ModuleTreeTableItemData(moduleData.getModuleName(), "", serverStatus, moduleData.getServerName(), moduleData.getServerLogPath()));
                    ModuleTreeTableItemDataList.forEach(moduleTreeTableItemData -> moduleRoot.getChildren().add(new TreeItem<>(moduleTreeTableItemData)));
                    rootNode.getChildren().add(moduleRoot);
                }
                Platform.runLater(() -> {
                    moduleTreeTable.setRoot(rootNode);
                    moduleTreeTable.setShowRoot(false);
                    loadingRoot.setVisible(false);
                });
            }
        });
    }

    /**
     * 服务状态包装
     *
     * @param isRunning 状态
     * @return 包装string
     */
    private String serverStatusWrapper(boolean isRunning) {
        return (isRunning ? "运行" : "停止");
    }

    /**
     * 初始化未安装服务的数据
     */
    private void initNotInstallData(TreeItem<NavigationTreeItemData> notInstallRoot) {
        notInstallSet.clear();
        Set<String> moduleNameSet = installConfig.getModuleContainerNames();
        notInstallSet.addAll(moduleNameSet);
        for (ModuleData moduleData : installedModuleList) {
            if (moduleNameSet.contains(moduleData.getModuleName())) {
                notInstallSet.remove(moduleData.getModuleName());
            }
        }
        System.out.println("当前未安装的服务个数:" + notInstallSet.size());
        for (String name : notInstallSet) {
            notInstallRoot.getChildren().add(new TreeItem<>(new NavigationTreeItemData(GlobalData.NOT_INSTALL_TYPE, name, "", System.getProperty("user.dir") + File.separator + "package" + File.separator + name, "", "")));
        }
    }

    /**
     * 初始化相关属性
     */
    private void initData() {
        moduleConfigEdit.setWrapText(true);
        moduleLogSelect.setItems(FXCollections.observableArrayList("debug日志", "info日志"));
        windowsUserName.setText(System.getProperty("user.name"));
        navigationTreeView.getSelectionModel().selectFirst();
        moduleLogSelect.getSelectionModel().selectFirst();
        moduleLogSelect.setEditable(false);
    }

    /**
     * 导航树监听
     */
    private void initListener() {
        navigationTreeView.setCellFactory(param -> new CustomTreeCell());
        navigationTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getValue() != null) {
                if (newValue.getValue().getType() == GlobalData.INSTALLED_ROOT_TYPE) {
                    if (moduleLogWatchDog != null) {
                        moduleLogWatchDog.interrupt();
                    }
                    restartService.setDisable(true);
                    startService.setDisable(true);
                    stopService.setDisable(true);
                    refreshService.setDisable(true);
                    if (!installedModuleList.isEmpty()) {
                        loadingRoot.setVisible(true);
                    }
                    installedRootPane.setVisible(true);
                    moduleConfigEditRoot.setVisible(false);
                    moduleLogRoot.setVisible(false);
                    serverStartLogRoot.setVisible(true);
                    notInstallRootPane.setVisible(false);
                    //刷新状态
                    initModuleTreeTableData();
                } else if (newValue.getValue().getType() == GlobalData.INSTALLED_TYPE) {
                    if (deployLogFileWatchdog != null) {
                        deployLogFileWatchdog.interrupt();
                    }
                    if (!installedModuleList.isEmpty()) {
                        loadingRoot.setVisible(true);
                    }
                    installedRootPane.setVisible(false);
                    moduleConfigEditRoot.setVisible(true);
                    moduleLogRoot.setVisible(true);
                    serverStartLogRoot.setVisible(false);
                    notInstallRootPane.setVisible(false);
                    editModuleConfigFile();
                    readModuleLog();
                    loadingRoot.setVisible(false);
                } else if (newValue.getValue().getType() == GlobalData.NOT_INSTALL_ROOT_TYPE || newValue.getValue().getType() == GlobalData.NOT_INSTALL_TYPE) {
                    if (moduleLogWatchDog != null) {
                        moduleLogWatchDog.interrupt();
                    }
                    if (deployLogFileWatchdog != null) {
                        deployLogFileWatchdog.interrupt();
                    }
                    notInstallRootPane.setVisible(true);
                    installedRootPane.setVisible(false);
                    moduleConfigEditRoot.setVisible(false);
                    moduleLogRoot.setVisible(false);
                    serverStartLogRoot.setVisible(false);
                    initNotInstallListView();
                }
            }
        });
        moduleTreeTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ModuleTreeTableItemData tableItemData = newValue.getValue();
                refreshService.setDisable(false);
                restartService.setDisable(true);
                startService.setDisable(true);
                stopService.setDisable(true);
                if (tableItemData.getStatus().equals("停止")) {
                    startService.setDisable(false);
                    restartService.setDisable(true);
                } else if (tableItemData.getStatus().equals("运行")) {
                    stopService.setDisable(false);
                    restartService.setDisable(false);
                }
            }
        });
        moduleLogSelect.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("日志级别更改");
            NavigationTreeItemData selectTreeData = navigationTreeView.getSelectionModel().getSelectedItem().getValue();
            String debugLogPath = selectTreeData.getDebugLogPath() + File.separator + "debug.log";
            System.out.println("debugLog位置:" + debugLogPath);
            String infoLogPath = selectTreeData.getDebugLogPath() + File.separator + "info.log";
            System.out.println("infoLog位置:" + infoLogPath);
            if (newValue.equals("debug日志")) {
//                    showModuleLog(debugLogPath);
                watchLogFile(debugLogPath);
            } else {
//                    showModuleLog(infoLogPath);
                watchLogFile(infoLogPath);
            }
        });
        installListView.setCellFactory(param -> new CustomListViewCell<>());
        nameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getName()));
        versionColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getVersion()));
        statusColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getStatus()));
    }

    /**
     * 编辑每个服务的moduleVersion.config
     */
    private void editModuleConfigFile() {
        NavigationTreeItemData treeItemData = navigationTreeView.getSelectionModel().selectedItemProperty().getValue().getValue();
        File configFile = new File(treeItemData.getPath() + File.separator + "moduleVersion.config");
        BufferedReader bufferedReader = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(configFile);
            bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String lineText;
            while ((lineText = bufferedReader.readLine()) != null) {
                stringBuilder.append(lineText);
                stringBuilder.append("\n");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            moduleConfigEdit.setText(stringBuilder.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 查看module下的debug和info日志
     */
    private void readModuleLog() {
        NavigationTreeItemData selectTreeData = navigationTreeView.getSelectionModel().getSelectedItem().getValue();
        String debugLogPath = selectTreeData.getDebugLogPath() + File.separator + "debug.log";
        String infoLogPath = selectTreeData.getDebugLogPath() + File.separator + "info.log";
        String selectedLevel = moduleLogSelect.getSelectionModel().getSelectedItem();
        if (selectedLevel.equals("debug日志")) {
//            showModuleLog(debugLogPath);
            watchLogFile(debugLogPath);
        } else {
//            showModuleLog(infoLogPath);
            watchLogFile(infoLogPath);
        }

    }

    /**
     * module下的日志文件变化监听
     *
     * @param path 日志文件路径
     */
    private void watchLogFile(String path) {
        if (!path.equals(tmpModulePath)) {
            tmpModulePath = path;
            if (moduleLogWatchDog != null) {
                moduleLogWatchDog.stop();
            }
            moduleLogWatchDog = new FileWatchdog(path) {
                @Override
                protected void doOnChange() {
                    System.out.println("当前显示的module日志路径:" + path);
                    showModuleLog(path);
                }
            };
            moduleLogWatchDog.setDelay(1000);
            moduleLogWatchDog.start();
        }
    }

    /**
     * 显示module下的debug或者info日志
     *
     * @param path 日志读取路径
     */
    private void showModuleLog(String path) {
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        InputStreamReader inputStreamReader = null;
        FileInputStream fileInputStream = null;
        BufferedReader bufferedReader = null;
        try {
            fileInputStream = new FileInputStream(path);
            inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            double deployIndex = moduleLogTextArea.getScrollTop();
            Platform.runLater(() -> {
                moduleLogTextArea.setText(stringBuilder.toString());
                moduleLogTextArea.selectEnd();
                moduleLogTextArea.deselect();
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化还未安装的列表数据
     */
    private void initNotInstallListView() {
        notInstallList.clear();
        for (String name : notInstallSet) {
            notInstallList.add(new ListViewCellData(name, false));
        }
        installListView.setItems(notInstallList);
        installListView.setCellFactory(param -> new CustomListViewCell<>());
        installListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    /**
     * 确认安装按钮点击
     *
     * @param actionEvent 点击事件
     */
    public void installConfirmClick(ActionEvent actionEvent) {
        Set<String> selectedInstallSet = new HashSet<>();
        for (ListViewCellData listViewCellData : notInstallList) {
            if (listViewCellData.isSelected()) {
                selectedInstallSet.add(listViewCellData.getName());
            }
        }
        System.out.println("选择要安装的服务个数:" + selectedInstallSet.size());
        if (selectedInstallSet.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("警告");
            alert.setHeaderText(null);
            alert.setContentText("还未勾选需要安装的服务");
            alert.showAndWait();
        } else if (windowsPwd.getText().trim().equals("") && OS_NAME.contains("win")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("警告");
            alert.setHeaderText(null);
            alert.setContentText("还未输入用户" + System.getProperty("user.name") + "的密码");
            alert.showAndWait();
        } else {
            if (OS_NAME.contains("win")) {
                System.out.println("选择windows安装的位置:" + Paths.get(installPathText.getText()));
                InstallUtils.installOnlinePackageForWindows(selectedInstallSet, installConfig, Paths.get(installPathText.getText()), tmpRunJarPath, windowsUserName.getText(), windowsPwd.getText());
            } else {
                System.out.println("选择linux安装的位置:" + Paths.get(installPathText.getText()));
                InstallUtils.installOnlinePackageForLinux(selectedInstallSet, installConfig, Paths.get(installPathText.getText()), tmpRunJarPath, windowsUserName.getText(), windowsPwd.getText());
            }
        }
    }

    /**
     * moduleVersion.config文件保存按钮事件
     *
     * @param actionEvent
     */
    public void saveEditClick(ActionEvent actionEvent) {
        String nowEditText = moduleConfigEdit.getText();
        NavigationTreeItemData selectTreeData = navigationTreeView.getSelectionModel().getSelectedItem().getValue();
        File moduleFile = new File(selectTreeData.getPath() + File.separator + "moduleVersion.config");
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(moduleFile);
            fileOutputStream.write(nowEditText.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ServerUtils.restartService(selectTreeData.getServer());
    }

    /**
     * moduleVersion.config文件重新加载按钮事件
     *
     * @param actionEvent
     */
    public void reloadEditClick(ActionEvent actionEvent) {
        editModuleConfigFile();
    }

    /**
     * 开启服务
     *
     * @param actionEvent
     */
    public void openServiceClick(ActionEvent actionEvent) {
        ModuleTreeTableItemData tableItemData = ((ModuleTreeTableItemData) moduleTreeTable.getSelectionModel().getSelectedItem().getValue());
        System.out.println("开启服务名:" + tableItemData.getServer());
        ServerUtils.startService(tableItemData.getServer());
        initDeployLog(tableItemData.getLogPath());
    }

    /**
     * 停止服务
     *
     * @param actionEvent
     */
    public void stopService(ActionEvent actionEvent) {
        ModuleTreeTableItemData tableItemData = ((ModuleTreeTableItemData) moduleTreeTable.getSelectionModel().getSelectedItem().getValue());
        System.out.println("停止服务名:" + tableItemData.getServer());
        ServerUtils.stopService(tableItemData.getServer());
        initDeployLog(tableItemData.getLogPath());
    }

    /**
     * 重启服务
     *
     * @param actionEvent
     */
    public void restartService(ActionEvent actionEvent) {
        ModuleTreeTableItemData tableItemData = ((ModuleTreeTableItemData) moduleTreeTable.getSelectionModel().getSelectedItem().getValue());
        System.out.println("重启服务名:" + tableItemData.getServer());
        ServerUtils.restartService(tableItemData.getServer());
        initDeployLog(tableItemData.getLogPath());
    }

    /**
     * 刷新服务状态
     *
     * @param actionEvent
     */
    public void refreshService(ActionEvent actionEvent) {
//        if (deployLogFileWatchdog != null) {
//            stopDeployLogWatch = true;
//        }
        if (deployLogFileWatchdog != null) {
            tmpDeployLogPath = "";
            stopDeployLogWatch = true;
            deployLogFileWatchdog.stop();
        }
        loadingRoot.setVisible(true);
        deployTextArea.setText("");
        startService.setDisable(true);
        stopService.setDisable(true);
        restartService.setDisable(true);
        refreshService.setDisable(true);
        initModuleTreeTableData();
    }

    /**
     * 选择服务安装位置
     *
     * @param actionEvent
     */
    public void installPathClick(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("安装位置");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File file = directoryChooser.showDialog(rootPane.getScene().getWindow());
        if (file != null) {
            installPathText.setText(file.getPath());
            System.out.println("选择的服务安装位置:" + Paths.get(file.getPath()).resolve("wcedla").toAbsolutePath());
        }
    }

    InputStreamReader deployLogInputStreamReader = null;
    FileInputStream deployFileInputStream = null;
    BufferedReader deployFileBufferedReader = null;

    /**
     * 服务启动停止等，由服务之星bat产生的日志读取
     *
     * @param path
     */
    private void initDeployLog(String path) {
        if (!tmpDeployLogPath.equals(path)) {
            tmpDeployLogPath = path;
            if (deployLogFileWatchdog != null) {
                stopDeployLogWatch = true;
                deployLogFileWatchdog.stop();
            }
            stopDeployLogWatch = false;
            deployLogFileWatchdog = new FileWatchdog(path) {
                @Override
                protected void doOnChange() {
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    try {
                        deployFileInputStream = new FileInputStream(path);
                        deployLogInputStreamReader = new InputStreamReader(deployFileInputStream, "gb2312");
                        deployFileBufferedReader = new BufferedReader(deployLogInputStreamReader);
                        while ((line = deployFileBufferedReader.readLine()) != null || stopDeployLogWatch) {
                            stringBuilder.append(line);
                            stringBuilder.append("\n");
                        }
                        double deployIndex = deployTextArea.getScrollTop();
                        Platform.runLater(() -> {
                            deployTextArea.setText(stringBuilder.toString());
                            deployTextArea.selectEnd();
                            deployTextArea.deselect();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            deployFileBufferedReader.close();
                            deployLogInputStreamReader.close();
                            deployFileInputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            deployLogFileWatchdog.setDelay(1000);
            deployLogFileWatchdog.start();
        }
    }

    /**
     * 摧毁
     */
    public void destroy() {
        if (future != null) {
            future.cancel(true);
        }
        initThreadPool.shutdownNow();
    }

    /**
     * 完成安装
     *
     * @param actionEvent
     */
    public void installOkClick(ActionEvent actionEvent) {
        installOkRefreshData();
    }

    /**
     * 刷新安装与未安装数据
     */
    private void installOkRefreshData() {
        System.out.println("刷新安装与未安装数据");
        loadingRoot.setVisible(true);
        TreeItem<NavigationTreeItemData> navigationTreeRoot = new TreeItem<>(new NavigationTreeItemData(-1, "", "", "", "", ""));
        TreeItem<NavigationTreeItemData> installedRoot = new TreeItem<>(new NavigationTreeItemData(GlobalData.INSTALLED_ROOT_TYPE, "已部署服务", "", "installedRoot", "", ""));
        TreeItem<NavigationTreeItemData> notInstallRoot = new TreeItem<>(new NavigationTreeItemData(GlobalData.NOT_INSTALL_TYPE, "待安装服务", "", "notInstallRoot", "", ""));
        installedRoot.setExpanded(true);
        notInstallRoot.setExpanded(true);
        File xmlConfigurationFile = new File(GlobalData.INSTALLED_XML_FILE_PATH);
        System.out.println("部署配置文件路径:" + xmlConfigurationFile.getPath() + ",是否存在:" + xmlConfigurationFile.exists());
        if (xmlConfigurationFile.exists()) {
            getInstalledData(installedRoot);
            if (!installedModuleList.isEmpty()) {
                initModuleTreeTableData();
            }
        }
        initNotInstallData(notInstallRoot);
        navigationTreeRoot.getChildren().add(installedRoot);
        navigationTreeRoot.getChildren().add(notInstallRoot);
        navigationTreeView.setRoot(navigationTreeRoot);
        navigationTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        navigationTreeView.setShowRoot(false);
        notInstallList.clear();
        for (String name : notInstallSet) {
            notInstallList.add(new ListViewCellData(name, false));
        }
        installListView.setItems(notInstallList);
        installListView.setCellFactory(param -> new CustomListViewCell<>());
        loadingRoot.setVisible(false);
    }

}
