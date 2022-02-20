package org.ajoberstar.gradle.stutter;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

public abstract class StutterExtension {
  public abstract RegularFileProperty getLockFile();

  public abstract NamedDomainObjectContainer<StutterMatrix> getMatrices();

  public abstract Property<Boolean> getSparse();
}
