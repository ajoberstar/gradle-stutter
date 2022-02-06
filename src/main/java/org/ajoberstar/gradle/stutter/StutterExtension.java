package org.ajoberstar.gradle.stutter;

import java.util.regex.Pattern;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class StutterExtension {
  private static final Pattern JAVA_MAJOR_VERSION = Pattern.compile("^(?:1\\.)?(\\d+)(?:[\\._-]|$)");

  private final ObjectFactory objectFactory;

  private final RegularFileProperty lockFile;
  private final NamedDomainObjectContainer<StutterMatrix> matrices;
  private final Property<Boolean> sparse;

  @Inject
  public StutterExtension(ObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
    this.lockFile = objectFactory.fileProperty();
    this.matrices = objectFactory.domainObjectContainer(StutterMatrix.class);
    this.sparse = objectFactory.property(Boolean.class);
  }

  public RegularFileProperty getLockFile() {
    return lockFile;
  }

  public NamedDomainObjectContainer<StutterMatrix> getMatrices() {
    return matrices;
  }

  public void matrices(Action<? super NamedDomainObjectContainer<? super StutterMatrix>> configureAction) {
    configureAction.execute(matrices);
  }

  public Property<Boolean> getSparse() {
    return sparse;
  }
}
