package io.github.avatarhurden.lifeorganizer.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Modality;

import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;
import org.json.JSONObject;

import com.littlebigberry.httpfiledownloader.FileDownloader;
import com.littlebigberry.httpfiledownloader.FileDownloaderDelegate;

public class Updater implements FileDownloaderDelegate {

	private NotificationPane pane;
	ProgressIndicator p;
	
    private double currentVersion, latestVersion;
    private String latestChanges;
    private String fileLocation;

    public Updater(double currentVersion, String url, NotificationPane pane) {
        this.currentVersion = currentVersion;
        this.pane = pane;
        
        try {
        	JSONObject changeLog = getChangeLog(new URL(url));
        
        this.latestVersion = changeLog.getJSONObject("current").getDouble("version");
        this.latestChanges = changeLog.getJSONObject("current").getString("changes");
        this.fileLocation = changeLog.getJSONObject("current").getString("url");
        System.out.println(fileLocation);
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
    }

    private JSONObject getChangeLog(URL url) throws IOException {
    	StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        int cp;
        while ((cp = reader.read()) != -1) {
            sb.append((char) cp);
        }
        return new JSONObject(sb.toString());
    }
    
    public void start() {
//        if (Config.<Boolean>getProperty("ask_for_updates", s -> Boolean.valueOf(s), false)) {
//            return;
//        }
    	if (currentVersion >= latestVersion)
    		return;

		pane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
		
		pane.getActions().add(new Action("Changes", event -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.initModality(Modality.NONE);
			alert.setTitle("Mudanças da versão nova");
			alert.setHeaderText(null);
			alert.setContentText(this.latestChanges);
			alert.show();
		}));
			

		System.out.println(Thread.currentThread());
		
		pane.getActions().add(new Action("Yes", event -> {
			p = new ProgressIndicator();
			pane.setGraphic(new ProgressIndicator());
			beginDownload();
		}));
		pane.getActions().add(new Action("No", event -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.initModality(Modality.APPLICATION_MODAL);
			alert.setTitle("Novas atualizações");
			alert.setContentText("Deseja receber novas atualizações no futuro?");
			ButtonType sim = new ButtonType("Sim");
			ButtonType nao = new ButtonType("Não");
			alert.getButtonTypes().setAll(sim, nao);
			Optional<ButtonType> result = alert.showAndWait();
			
			if (result.get() == nao)
				System.out.println("no more");
			
			pane.hide();
		}));
		
		System.out.println("Hi");
		pane.show("Do you wish to update from version " + this.currentVersion + " to version " + this.latestVersion + "?");
        
    }


    /**
     * Inicia o download usando a url remota e baixando no local no jar sendo executado.
     */
    private void beginDownload() {
        FileDownloader fileDownloader = new FileDownloader(this);
        fileDownloader.setUrl(fileLocation);
        fileDownloader.setLocalLocation("test.jar"); //Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        fileDownloader.beginDownload();
    }

    @Override
    public void didStartDownload(FileDownloader fileDownloader) {
    	pane.setGraphic(p);
    }

    @Override
    public void didProgressDownload(FileDownloader fileDownloader) {
    	Platform.runLater(() -> {
    		p.setProgress(Double.valueOf(fileDownloader.getPercentComplete().replace("%", "").replace(",", "."))/100.0);
    	});
    }

    @Override
    public void didFinishDownload(FileDownloader fileDownloader) {
		Platform.runLater(() -> {
			pane.getActions().clear();
		});
    }

    @Override
    public void didFailDownload(FileDownloader fileDownloader) {
    	System.out.println(fileDownloader.getPercentComplete());
		System.out.println(Thread.currentThread());
    }
}
