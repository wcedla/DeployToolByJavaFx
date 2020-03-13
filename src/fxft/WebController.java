//package fxft;
//
//import com.teamdev.jxbrowser.chromium.Browser;
//import com.teamdev.jxbrowser.chromium.events.*;
//import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
//
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.Button;
//import javafx.scene.control.TextField;
//import javafx.scene.layout.AnchorPane;
//
//
//import java.net.URL;
//import java.util.ResourceBundle;
//
//public class WebController implements Initializable {
//
//    @FXML
//    public AnchorPane browserRoot;
//    @FXML
//    public TextField urlFiled;
//    @FXML
//    public BrowserView browserView;
//
//    @FXML
//    public Button gotUrl;
//
//    Browser browser;
//
//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
////        webBrowser.setContextMenuEnabled(true);
////        WebEngine webEngine=webBrowser.getEngine();
////        webEngine.load("https://www.baidu.com");
////        webEngine.setJavaScriptEnabled(true);
////        webEngine.getLoadWorker().progressProperty().addListener(new ChangeListener<Number>() {
////            @Override
////            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
////                System.out.println("加载进度:"+newValue.floatValue());
////            }
////        });
////        webBrowser.setCache(true);
////        webEngine.locationProperty().addListener(new ChangeListener<String>() {
////            @Override
////            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
////                System.out.println("新地址:"+newValue);
////                webEngine.load(newValue);
////            }
////        });
//        browser = browserView.getBrowser();
//        browser.loadURL("http://igps.austar.cc/");
//        urlFiled.setText(browser.getURL());
//        browser.addLoadListener(new LoadAdapter() {
//            @Override
//            public void onStartLoadingFrame(StartLoadingEvent event) {
//                super.onStartLoadingFrame(event);
//                if (event.isMainFrame()) {
//                    urlFiled.setText(event.getValidatedURL());
//                }
//            }
//        });
//    }
//
//    public void goUrlClick(ActionEvent actionEvent) {
//        if(browser!=null){
//            browser.loadURL(urlFiled.getText());
//        }
//    }
//
//}
