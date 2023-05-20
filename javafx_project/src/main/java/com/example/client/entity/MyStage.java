package com.example.client.entity;

public class MyStage {
    public enum Stages {
        ADD_PLAYERS,
        ADD_PLAYER,
        END_GAME,
        UPDATE,
        SCORE
    }

    public Stages stage;

    public MyStage(Stages stage) {
        this.stage = stage;
    }

    public Stages action() {
        return stage;
    }
}
