package com.ckroetsch.hanabi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author curtiskroetsch
 */
public final class Game {

    @JsonProperty("numCardsRemaining")
    int mCardsRemaining;

    @JsonProperty("numPlayers")
    int mNumPlayers;

    @JsonProperty("isRainbow")
    boolean mIsRainbow;

    @JsonProperty("numHints")
    int mHints;

    @JsonProperty("numLives")
    int mLives;

    @JsonProperty("players")
    List<Player> mPlayers;

    @JsonProperty("played")
    State mState;

    @JsonProperty("discarded")
    List<Card> mDiscarded;

    private Game() {

    }

    public int getCardsRemaining() {
        return mCardsRemaining;
    }

    public int getNumPlayers() {
        return mNumPlayers;
    }

    public boolean isRainbow() {
        return mIsRainbow;
    }

    public int getHints() {
        return mHints;
    }

    public int getLives() {
        return mLives;
    }

    public List<Player> getPlayers() {
        return mPlayers;
    }

    public State getState() {
        return mState;
    }

    public List<Card> getDiscards() {
        return mDiscarded;
    }

}
