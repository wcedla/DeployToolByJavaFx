package fxft;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.URL;

public class App extends Application {

//    static {
//        try {
//            Field e = be.class.getDeclaredField("e");
//            e.setAccessible(true);
//            Field f = be.class.getDeclaredField("f");
//            f.setAccessible(true);
//            Field modifersField = Field.class.getDeclaredField("modifiers");
//            modifersField.setAccessible(true);
//            modifersField.setInt(e, e.getModifiers() & ~Modifier.FINAL);
//            modifersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
//            e.set(null, new BigInteger("1"));
//            f.set(null, new BigInteger("1"));
//            modifersField.setAccessible(false);
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
//    }

    Parent root;

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL url = getClass().getResource("Home.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(url);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        root = fxmlLoader.load();
        primaryStage.setTitle("一键部署安装工具");
        Scene scene = new Scene(root);
        scene.getStylesheets().add("fxft/css/listview.css");
        primaryStage.setScene(scene);
        HomeController homeController = fxmlLoader.getController();
        primaryStage.setOnCloseRequest(event -> homeController.destroy());
        primaryStage.show();
    }
//
//    private void closeBrowser() {
//        String osName = System.getProperty("os.name").toLowerCase();
//        if (osName.contains("win")) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    ((BrowserView) root.lookup("#browserView")).getBrowser().dispose();
//                }
//            }).start();
//        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    ((BrowserView) root.lookup("#browserView")).getBrowser().dispose();
//                }
//            });
//        } else if (osName.contains("mac")) {
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    ((BrowserView) root.lookup("#browserView")).getBrowser().dispose();
//                }
//            });
//        } else if (osName.contains("sunos")) {
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    ((BrowserView) root.lookup("#browserView")).getBrowser().dispose();
//                }
//            });
//        }
//    }


    public static void main(String[] args) {
        launch(args);
    }
}
