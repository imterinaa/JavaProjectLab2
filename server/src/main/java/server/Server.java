package server;

import DBconnection.DataBase;
import com.google.gson.Gson;
import entity.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class Server {
    private static final int port = 8080;
    DataBase db;
    private final List<Socket> clientSockets = new ArrayList<>(4);
    private final List<DataInputStream> clientSocketsIn = new ArrayList<>(4);
    private final List<DataOutputStream> clientSocketsOut = new ArrayList<>(4);
    private final List<Arrow> arrows = new ArrayList<>(4);
    private final List<Target> targets = new ArrayList<>(2);
    private final boolean[] shotsHandler = new boolean[4];
    private final int[] shotsCounter = new int[4];
    private final int[] scoreCounter = new int[4];
    private final double[] layouts = new double[4];
    private final PlayersList playersList = new PlayersList();
    private final ServerSocket srv;
    private String winner = "";
    ArrayList<Pair> listofpair = new ArrayList<>();
    private final AtomicBoolean isStopped = new AtomicBoolean(false);
    private final AtomicBoolean stopAccept = new AtomicBoolean(false);
    private final AtomicInteger startConfirmCount = new AtomicInteger(0);
    Pair p = new Pair(0,winner);
    public Server() throws IOException {
        srv = new ServerSocket(port);
        targets.add(new Target(5, 30, 446));
        targets.add(new Target(10, 15, 500));
    }

    void setLayouts() {
        switch (clientSockets.size()) {
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
    public void Begin(DataBase db) {
        this.db = db;

    }
    public void start() throws InterruptedException {
        DataBase db = new DataBase();
        Begin(db);
        Thread acceptConn = new Thread(() -> {
            while (clientSockets.size() < 4) {
                try {
                    Socket socket = srv.accept();
                    if (stopAccept.get()) {
                        break;
                    }
                    clientSockets.add(socket);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());

                    clientSocketsIn.add(in);
                    clientSocketsOut.add(out);
                    arrows.add(new Arrow(535));

                    String name = in.readUTF();
                    playersList.players().add(validateName(name));
                    db.addPlayer(name);
                    out.writeUTF(new Gson().toJson(new Stage(Stage.Stages.ADD_PLAYERS)));
                    out.flush();

                    out.writeUTF(new Gson().toJson(playersList));
                    out.flush();

                    broadcast(new Stage(Stage.Stages.ADD_PLAYER));
                    int num = clientSockets.size() - 1;
                    listenSocket(num);
                } catch (IOException ignored) {
                }
            }
        });

        acceptConn.start();

        while (startConfirmCount.get() != clientSockets.size() || startConfirmCount.get() == 0) {
        }
        acceptConn.interrupt();
        stopAccept.set(true);

        new Thread(()-> {
            setLayouts();
            while (true) {
                while (isStopped.get()) {
                }
                if (startConfirmCount.get() == clientSockets.size()) {
                    try {
                        if (!Objects.equals(getWinner(), "")) {
                            resetData();
                            broadcast(new Stage(Stage.Stages.UPDATE));
                            broadcast(new Stage(Stage.Stages.END_GAME));
                            isStopped.set(true);
                            startConfirmCount.set(0);
                            continue;
                        }
                        broadcast(new Stage(Stage.Stages.UPDATE));
                        Thread.sleep(30);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    private String validateName(String name) {
        String tmp = name;
        int counter = 0;
        while (true) {
            if (playersList.players().contains(tmp)) {
                tmp += Integer.toString(counter++);
            } else {
                return tmp;
            }
        }
    }

    private String getWinner() {
        for (int i = 0; i < 4; i++) {
            if (scoreCounter[i] >= 2) {
                winner = playersList.players().get(i);


            }
        }
        return winner;
    }

    private void resetData() {
        for (int i = 0; i < 4; i++) {
            scoreCounter[i] = 0;
            shotsCounter[i] = 0;
            shotsHandler[i] = false;
        }
        targets.set(0, new Target(5, 20, 446));
        targets.set(1, new Target(10, 10, 500));
        for(var arrow : arrows) {
            arrow.rollback();
        }
    }

    private void listenSocket(int num) {
        DataInputStream in = clientSocketsIn.get(num);
        DataOutputStream out = clientSocketsOut.get(num);
        new Thread(() -> {
            while (true) {
                try {
                    String event = in.readUTF();
                    System.out.println(event);
                    switch (event) {
                        case "shot" -> {
                            if (!shotsHandler[num] && !isStopped.get()) {
                                shotsHandler[num] = true;
                                shotsCounter[num]++;
                            }
                        }
                        case "stop" -> {
                            if(!isStopped.get()) {
                                isStopped.set(true);
                                startConfirmCount.set(0);
                            }
                        }
                        case "start" -> {
                            startConfirmCount.incrementAndGet();
                            if (startConfirmCount.get() == clientSockets.size()) {
                                isStopped.set(false);
                            }
                        }
                        case "get_winners" -> {
                            Stage stage = new Stage(Stage.Stages.SCORE);

                            out.writeUTF(new Gson().toJson(stage));
                            out.flush();

                            out.writeUTF(new Gson().toJson(db.getAllPlayers()));
                            out.flush();
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void broadcast(Stage stage) throws IOException {

        switch (stage.action()) {
            case ADD_PLAYER -> {
                for (int i = 0; i < clientSocketsOut.size() - 1 ; i++) {
                    int size = playersList.players().size();
                    PlayersList temp = new PlayersList();
                    temp.players().add(playersList.players().get(size - 1));
                    clientSocketsOut.get(i).writeUTF(new Gson().toJson(stage));
                    clientSocketsOut.get(i).flush();

                    clientSocketsOut.get(i).writeUTF(new Gson().toJson(temp));
                    clientSocketsOut.get(i).flush();
                }
            }
            case END_GAME -> {
                if (!listofpair.isEmpty()) {
                    Iterator<Pair> iterator = listofpair.iterator();
                    while (iterator.hasNext()) {
                        Pair pair = iterator.next();
                        if (pair.getString().equals(winner)) {
                            pair.SetInt();
                            pair.SetString(winner);
                        }
                        else {
                            listofpair.add(new Pair(1, winner));
                        }
                    }

                } else {
                    listofpair.add(new Pair(1, winner));
                }

                System.out.println("privet");
                //listofpair.add(p);
                db.setPlayerWins(listofpair.get(listofpair.size()-1));
                for(var out : clientSocketsOut) {
                    out.writeUTF(new Gson().toJson(stage));
                    out.flush();
//                    for (Pair pair : listofpair) {
//                        if (pair.getString().equals(winner)) {
//                            pair.SetInt();
//                            System.out.println(pair.getInt());
//                            db.setPlayerWins(pair);
//                            break;
//                        }
//                        else {listofpair.add(new Pair(1,winner));
//                            db.setPlayerWins(listofpair.get(listofpair.size()-1));
//                            System.out.println(pair.getInt());
//                            break;
//                        }}

                    out.writeUTF(new Gson().toJson(winner));
                    out.flush();
                }
                winner = "";
            }
            case UPDATE -> {
                Update update = new Update();
                for (int i = 0; i < clientSockets.size(); i++) {
                    if (shotsHandler[i]){
                        if (!arrows.get(i).isEnd()){
                            double x = arrows.get(i).move();
                            for(var target : targets) {
                                if (target.isInTarget(x, layouts[i])) {
                                    arrows.get(i).rollback();
                                    shotsHandler[i] = false;
                                    scoreCounter[i]++;
                                }
                            }
                        } else {
                            arrows.get(i).rollback();
                            shotsHandler[i] = false;
                        }
                    }
                    update.projectileXCoords.add(arrows.get(i).x());
                    update.shotsList().add(shotsCounter[i]);
                    update.scoreList().add(scoreCounter[i]);
                }

                for(var target : targets) {
                    update.targetYCoords.add(target.move());
                }

                for(var out : clientSocketsOut) {
                    out.writeUTF(new Gson().toJson(stage));
                    out.flush();

                    out.writeUTF(new Gson().toJson(update));
                    out.flush();
                }
            }
            case SCORE ->{
                for(var out : clientSocketsOut) {
                    Gson gson = new Gson();
                    String json = gson.toJson(db.getAllPlayers());
                    out.writeUTF(new Gson().toJson(json));
                    out.flush();
                }
            }
        }
    }
}
