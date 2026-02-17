package net.fg83.hytalkclient.service;

import java.time.Instant;

public class PairingManager {
    private String pairingCode;
    private Instant pairingExpiration;

    public String getPairingCode() { return pairingCode; }
    public void setPairingCode(String pairingCode) { this.pairingCode = pairingCode; }

    public Instant getPairingExpiration() { return pairingExpiration; }
    public void setPairingExpiration(Instant expiration) {this.pairingExpiration = expiration;}
}
