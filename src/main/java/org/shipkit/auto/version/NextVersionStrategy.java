package org.shipkit.auto.version;

import java.util.Optional;

public enum NextVersionStrategy {

    COUNT_COMMITS,
    SEQUENTIAL;

    public static Optional<NextVersionStrategy> parse(String strategy) {
        switch (strategy) {
            case "sequential":
                return Optional.of(NextVersionStrategy.SEQUENTIAL);
            case "commits":
                return Optional.of(NextVersionStrategy.COUNT_COMMITS);
            default:
                return Optional.empty();
        }
    }

    public String configValue() {
        switch (this) {
            case SEQUENTIAL:
                return "sequential";
            case COUNT_COMMITS:
                return "commits";
            default:
                throw new IllegalArgumentException("Unknown next version strategy " + this);
        }
    }

}
