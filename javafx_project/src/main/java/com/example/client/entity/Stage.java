package com.example.client.entity;

public class Stage {
    public enum Stages {
        ADD_PLAYERS,
        ADD_PLAYER,
        END_GAME,
        UPDATE,
    }

    public Stages stage;

    public Stage(Stages stage) {
        this.stage = stage;
    }

    public Stages action() {
        return stage;
    }
}
