package com.fifa.app.Entities;

public class GoalRequest {
    private String clubId;
    private String scorerIdentifier;
    private int minuteOfGoal;
    private boolean ownGoal;

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public String getScorerIdentifier() {
        return scorerIdentifier;
    }

    public void setScorerIdentifier(String scorerIdentifier) {
        this.scorerIdentifier = scorerIdentifier;
    }

    public int getMinuteOfGoal() {
        return minuteOfGoal;
    }

    public void setMinuteOfGoal(int minuteOfGoal) {
        this.minuteOfGoal = minuteOfGoal;
    }

    public boolean isOwnGoal() {
        return ownGoal;
    }

    public void setOwnGoal(boolean ownGoal) {
        this.ownGoal = ownGoal;
    }
}
