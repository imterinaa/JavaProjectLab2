package com.example.client.controller;

import com.example.client.entity.Pair;
import com.example.client.entity.MyStage;
import com.example.client.entity.PlayersList;
import com.example.client.entity.Update;
import com.example.client.service.ShapesLoader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameController {
    @FXML
    private Circle targetBig;
    @FXML
    private Circle targetSmall;
    @FXML
    private TextField nameInput;
    @FXML
    private Pane signUpPane;
    @FXML
    private Pane winnerPane;
    @FXML
    private Label winnerLabel;
    @FXML
    private Pane gameOwner;
    private List<AnchorPane> projectileOwner = new ArrayList<>(4);
    @FXML
    private VBox playersOwner;
    @FXML
    private VBox statsTable;
    private final String host = "localhost";
    private final int port = 8080;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String name;
    private double[] layouts = new double[4];
    private boolean wasAdded = false;

    @FXML
    private void initialize() throws IOException {
        socket = new Socket(host, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }

    void setLayouts() {
        switch (playersOwner.getChildren().size()) {
            case 1 -> {
                layouts[0] = 153;
            }
            case 2 -> {
                layouts[0] = 123;
                layouts[1] = 193;
            }
            case 3 -> {
                layouts[0] = 87;
                layouts[1] = 156;
                layouts[2] = 227;
            }
            case 4 -> {
                layouts[0] = 56;
                layouts[1] = 126;
                layouts[2] = 196;
                layouts[3] = 263;
            }
        }
    }

    @FXML
    protected void onStartButtonClick() throws InterruptedException, IOException {
        if (!wasAdded) {
            setLayouts();
            for (int i = 0; i < playersOwner.getChildren().size(); i++) {
                AnchorPane l = ShapesLoader.loadLine("arrow.fxml");
                l.setLayoutY(layouts[i]);
                l.setLayoutX(96.0);
                l.setVisible(false);
                projectileOwner.add(l);
                gameOwner.getChildren().add(l);
            }
            wasAdded = true;
        }
        out.writeUTF("start");
        out.flush();
    }

    @FXML
    protected void onStopButtonClick() throws IOException {
        out.writeUTF("stop");
        out.flush();
    }

    @FXML
    protected void getLeaders()throws IOException {
        out.writeUTF("get_winners");
        out.flush();
    }

    @FXML
    protected void onOkButtonClick() throws IOException {
        winnerPane.setVisible(false);
    }

    @FXML
    protected void onShotButtonClick() throws IOException {
        out.writeUTF("shot");
        out.flush();
    }

    @FXML
    protected void onSaveButtonClick() throws IOException {
        name = nameInput.getText();
        out.writeUTF(name);
        out.flush();

        new Thread(() -> {
            boolean clientPlayerAdded = false;
            while (true) {
                try {
                    MyStage myStage = new Gson().fromJson(in.readUTF(), MyStage.class);
                    switch (myStage.action()) {
                        case ADD_PLAYERS -> {
                            String data = in.readUTF();
                             PlayersList list = new Gson().fromJson(data, PlayersList.class);
                            for (int i = 0; i < list.players().size() - 1; i++) {
                                HBox hBox = ShapesLoader.loadHBox("player-state.fxml");
                                Label label = (Label) hBox.getChildren().get(1);
                                label.setText(list.players().get(i));
                                Platform.runLater(
                                        () -> statsTable.getChildren().add(hBox)
                                );

                                Polyline pl = ShapesLoader.loadPolyline("triangle.fxml");
                                Platform.runLater(
                                        () -> playersOwner.getChildren().add(pl)
                                );
                            }
                            HBox hBox = ShapesLoader.loadHBox("player-state.fxml");
                            Label label = (Label) hBox.getChildren().get(1);
                            label.setText(list.players().get(list.players().size() - 1));
                            Platform.runLater(
                                    () -> statsTable.getChildren().add(hBox)
                            );

                            Polyline pl = ShapesLoader.loadPolyline("select-triangle.fxml");
                            Platform.runLater(
                                    () -> playersOwner.getChildren().add(pl)
                            );
                        }
                        case ADD_PLAYER -> {
                            String data = in.readUTF();
                            PlayersList list = new Gson().fromJson(data, PlayersList.class);
                            HBox hBox = ShapesLoader.loadHBox("player-state.fxml");
                            Label label = (Label) hBox.getChildren().get(1);
                            label.setText(list.players().get(0));
                            Platform.runLater(
                                    () -> statsTable.getChildren().add(hBox)
                            );

                            Polyline pl = ShapesLoader.loadPolyline("triangle.fxml");
                            Platform.runLater(
                                    () -> playersOwner.getChildren().add(pl)
                            );
                        }
                        case END_GAME -> {
                            String winner = in.readUTF();
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Игра окончена");
                                alert.setHeaderText("...");
                                alert.setContentText("Победитель : " + winner + "!");
                                alert.getButtonTypes().add(ButtonType.CANCEL);
                                Window window = alert.getDialogPane().getScene().getWindow();
                                window.setOnCloseRequest(e -> alert.hide());
                                alert.showAndWait();
                            });

                        }
                        case UPDATE -> {
                            Update update = new Gson().fromJson(in.readUTF(), Update.class);
                            targetBig.setLayoutY(update.targetYCoords.get(0));
                            targetSmall.setLayoutY(update.targetYCoords.get(1));

                            for (int i = 0; i < playersOwner.getChildren().size(); i++) {
                                AnchorPane l = projectileOwner.get(i);

                                HBox hBox = (HBox) statsTable.getChildren().get(i);
                                Label shots = (Label) hBox.getChildren().get(3);
                                Label score = (Label) hBox.getChildren().get(5);

                                int finalI = i;
                                Platform.runLater(
                                        () -> {
                                            l.setVisible(update.projectileXCoords.get(finalI) > 96.0);
                                            l.setLayoutX(update.projectileXCoords.get(finalI));

                                            shots.setText(update.shotsList.get(finalI).toString());
                                            score.setText(update.scoreList.get(finalI).toString());
                                        }
                                );

                            }
                        }
                        case SCORE ->{
                            String data = in.readUTF();
                            Gson gson = new Gson();
                            ArrayList<Pair> arrayList = gson.fromJson(data, new TypeToken<ArrayList<Pair>>(){}.getType());
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    TableView tableView = new TableView();

                                    TableColumn<Pair, String> column1 =
                                            new TableColumn<>("Имя");

                                    column1.setCellValueFactory(
                                            new PropertyValueFactory<>("s"));


                                    TableColumn<Pair, String> column2 =
                                            new TableColumn<>("Победы");

                                    column2.setCellValueFactory(
                                            new PropertyValueFactory<>("i"));

                                    tableView.getColumns().add(column1);
                                    tableView.getColumns().add(column2);

                                    arrayList.forEach(tableView.getItems()::add);

                                    VBox vbox = new VBox(tableView);
                                    Scene scene = new Scene(vbox);
                                    Stage myStage = new Stage();
                                    myStage.setScene(scene);
                                    myStage.setTitle("Таблица лидеров");
                                    myStage.show();
                                }
                            });
                            //ArrayList<Pair> p = new Gson().fromJson(data, ArrayList.class);
                            ObservableList<Pair> observableList = FXCollections.observableArrayList();
                            System.out.println(arrayList);

                        }
                    }
                } catch (IOException ignored) {
                }
            }
        }).start();

        signUpPane.setVisible(false);
    }

}
