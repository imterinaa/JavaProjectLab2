package entity;

public class Stage {
    public enum Stages {
        ADD_PLAYERS,
        ADD_PLAYER,
        UPDATE,
        END_GAME,
        SCORE,
        WINNERS
    }

    public Stages stage;

    public Stage(Stages stage) {
        this.stage= stage;
    }

    public Stages action() {
        return stage;
    }
}
