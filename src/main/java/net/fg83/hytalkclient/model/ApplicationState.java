package net.fg83.hytalkclient.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ApplicationState {
    private String clientPairingCode;
    private VoiceChatPlayer clientPlayer;
    private Map<UUID, VoiceChatPlayer> voiceChatPlayers = new HashMap<>();
    private Instant pairingExpiration;

    public ApplicationState() { }

    public String getPairingCode() { return clientPairingCode; }
    public void setPairingCode(String pairingCode) { this.clientPairingCode = pairingCode; }
    public void setPlayer(VoiceChatPlayer player) {
        this.clientPlayer = player;
        this.clientPlayer.setLocalUser(true);
    }
    public VoiceChatPlayer getPlayer(){
        return this.clientPlayer;
    }
    public String getPlayerName() { return clientPlayer.getPlayerName(); }
    public Instant getPairingExpiration() { return pairingExpiration; }
    public void setPairingExpiration(Instant expiration) {this.pairingExpiration = expiration;}

    public void updateVoiceChatPlayers(Map<UUID, VoiceChatPlayer> newVoiceChatPlayers) {
        newVoiceChatPlayers.forEach((uuid, player) -> {
            if (this.voiceChatPlayers.containsKey(uuid)) {
                this.voiceChatPlayers.get(uuid).setPlayerLocation(player.getPlayerLocation());
            }
            else {
                if (player.getPlayerId().equals(this.clientPlayer.getPlayerId())) {
                    this.clientPlayer.setPlayerLocation(player.getPlayerLocation());
                }
                else {
                    this.voiceChatPlayers.put(uuid, player);
                }
            }
        });
        this.voiceChatPlayers.forEach((uuid, player) -> {
            if (!newVoiceChatPlayers.containsKey(uuid)) {
                this.voiceChatPlayers.remove(uuid);
            }
        });
    }
    public Map<UUID, VoiceChatPlayer> getVoiceChatPlayers() { return voiceChatPlayers; }
}