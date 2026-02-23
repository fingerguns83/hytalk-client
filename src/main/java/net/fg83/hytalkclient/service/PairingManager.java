// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.service;

import java.time.Instant;

/**
 * Manages pairing codes and their expiration for device pairing functionality.
 * Stores the current pairing code and tracks when it expires.
 */
public class PairingManager {
    // The current pairing code used for device pairing
    private String pairingCode;
    // The timestamp when the current pairing code expires
    private Instant pairingExpiration;

    /**
     * Gets the current pairing code.
     *
     * @return The pairing code, or null if not set
     */
    public String getPairingCode() {
        return pairingCode;
    }

    /**
     * Sets the pairing code.
     *
     * @param pairingCode The pairing code to set
     */
    public void setPairingCode(String pairingCode) {
        this.pairingCode = pairingCode;
    }

    /**
     * Gets the expiration timestamp for the current pairing code.
     *
     * @return The expiration instant, or null if not set
     */
    public Instant getPairingExpiration() {
        return pairingExpiration;
    }

    /**
     * Sets the expiration timestamp for the pairing code.
     *
     * @param expiration The expiration instant to set
     */
    public void setPairingExpiration(Instant expiration) {
        this.pairingExpiration = expiration;
    }
}