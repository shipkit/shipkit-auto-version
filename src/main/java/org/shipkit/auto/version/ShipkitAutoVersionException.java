package org.shipkit.auto.version;

/**
 * Base exception that has a clue generating common message prefix
 */
class ShipkitAutoVersionException extends RuntimeException {

    ShipkitAutoVersionException(String message, Throwable cause) {
        super("[shipkit-auto-version] " + message, cause);
    }

    ShipkitAutoVersionException(String message) {
        super("[shipkit-auto-version] " + message);
    }
}
