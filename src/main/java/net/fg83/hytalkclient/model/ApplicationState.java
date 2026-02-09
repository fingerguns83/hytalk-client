package net.fg83.hytalkclient.model;

import java.time.Instant;
import java.util.UUID;

public class ApplicationState {
    private String clientPairingCode;
    private String clientPlayerName;
    private UUID clientPlayerId;
    private Instant pairingExpiration;

    public ApplicationState() { }

    public String getPairingCode() { return clientPairingCode; }
    public void setPairingCode(String pairingCode) { this.clientPairingCode = pairingCode; }
    public String getPlayerName() { return clientPlayerName; }
    public void setPlayerName(String playerName) { this.clientPlayerName = playerName; }
    public UUID getPlayerId() { return clientPlayerId; }
    public void setPlayerId(UUID playerId) { this.clientPlayerId = playerId; }
    public Instant getPairingExpiration() { return pairingExpiration; }
    public void setPairingExpiration(Instant expiration) {this.pairingExpiration = expiration;}
}