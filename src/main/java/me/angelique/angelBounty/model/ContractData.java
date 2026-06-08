package me.angelique.angelBounty.model;

import java.util.UUID;

public class ContractData {

    public enum Type { PLAYER_KILL, STRUCTURE_DAMAGE, SHIPMENT_INTERCEPT, ESCORT }
    public enum Status { OPEN, ACCEPTED, COMPLETED, EXPIRED }

    private final String id;
    private final UUID posterUUID;
    private final String posterName;
    private final String target;
    private final Type type;
    private final double reward;
    private UUID acceptedBy;
    private Status status;
    private long postedAt;
    private long expiresAt;

    public ContractData(String id, UUID posterUUID, String posterName, String target, Type type, double reward) {
        this.id = id;
        this.posterUUID = posterUUID;
        this.posterName = posterName;
        this.target = target;
        this.type = type;
        this.reward = reward;
        this.status = Status.OPEN;
        this.postedAt = System.currentTimeMillis();
        this.expiresAt = System.currentTimeMillis() + 86400000L * 7; // 7 days
    }

    public String getId() { return id; }
    public UUID getPosterUUID() { return posterUUID; }
    public String getPosterName() { return posterName; }
    public String getTarget() { return target; }
    public Type getType() { return type; }
    public double getReward() { return reward; }
    public UUID getAcceptedBy() { return acceptedBy; }
    public void setAcceptedBy(UUID u) { this.acceptedBy = u; status = Status.ACCEPTED; }
    public Status getStatus() { return status; }
    public void setStatus(Status s) { this.status = s; }
    public long getPostedAt() { return postedAt; }
    public long getExpiresAt() { return expiresAt; }
    public boolean isExpired() { return System.currentTimeMillis() > expiresAt && status == Status.OPEN; }
}
