package me.angelique.angelBounty.model;

public final class WantedData {

    private int playerKills;
    private int stars;

    public WantedData() {
    }

    public WantedData(int playerKills, int stars) {
        setPlayerKills(playerKills);
        setStars(stars);
    }

    public int getPlayerKills() {
        return playerKills;
    }

    public void setPlayerKills(int playerKills) {
        this.playerKills = Math.max(0, playerKills);
    }

    public void addPlayerKill() {
        this.playerKills++;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = Math.max(0, stars);
    }
}
