package org.shipkit.auto.version;

import java.io.File;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSourceParameters;

public interface GitValueSourceParameters extends ValueSourceParameters {
    Property<File> getWorkingDirectory();
    Property<String[]> getCommands();
}
