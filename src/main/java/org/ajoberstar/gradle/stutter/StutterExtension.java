package org.ajoberstar.gradle.stutter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.util.GradleVersion;

public class StutterExtension {
  private static final Pattern JAVA_MAJOR_VERSION = Pattern.compile("^(?:1\\.)?(\\d+)(?:[\\._-]|$)");

  private final ObjectFactory objectFactory;

  private final DirectoryProperty lockDir;
  private final Map<Integer, StutterMatrix> matrices;
  private boolean sparse = false;

  public StutterExtension(Project project) {
    this.objectFactory = project.getObjects();
    this.lockDir = project.getObjects().directoryProperty();
    this.matrices = new HashMap<>();
  }

  public DirectoryProperty getLockDir() {
    return lockDir;
  }

  public StutterMatrix java(int version, Action<? super StutterMatrix> action) {
    StutterMatrix matrix = matrices.computeIfAbsent(version, v -> objectFactory.newInstance(StutterMatrix.class, version));
    action.execute(matrix);
    return matrix;
  }

  public boolean isSparse() {
    return sparse;
  }

  public void setSparse(boolean sparse) {
    this.sparse = sparse;
  }

  Set<GradleVersion> getLockedVersions() {
    return findCurrentLockFile().map(lockFile -> {
      try (Stream<String> lines = Files.lines(lockFile, StandardCharsets.UTF_8)) {
        return lines
            .filter(line -> !line.startsWith("#"))
            .map(String::trim)
            .map(GradleVersion::version)
            .collect(Collectors.toSet());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }).orElse(Collections.emptySet());
  }

  void writeLockFiles(Set<GradleVersion> allVersions) {
    // empty out directory
    try {
      Path lockDirPath = lockDir.get().getAsFile().toPath();
      Files.createDirectories(lockDirPath);
      Files.walkFileTree(lockDirPath, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
          if (!lockDirPath.equals(dir)) {
            Files.delete(dir);
          }
          return FileVisitResult.CONTINUE;
        }
      });

    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    matrices.values().forEach(matrix -> {
      Stream<String> headerLines = Stream.of("# DO NOT MODIFY: Generated by Stutter plugin.");
      Stream<String> versionLines = matrix.allCompatible(allVersions, sparse)
          .distinct()
          .sorted()
          .map(GradleVersion::getVersion);

      List<String> lines = Stream.concat(headerLines, versionLines).collect(Collectors.toList());

      try {
        Path lockFile = getLockFile(matrix.getJavaVersion());
        Files.write(lockFile, lines, StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

  private Optional<Path> findCurrentLockFile() {
    int javaVersion = StutterExtension.getCurrentJavaVersion();
    // Start from current version and work backwards until we find a defined matrix
    return IntStream.iterate(javaVersion, v -> v - 1)
        // can't use iterate with a hasNext predicate until Java 9
        .limit(javaVersion)
        .mapToObj(this::getLockFile)
        .filter(Files::exists)
        .findFirst();
  }

  private Path getLockFile(int javaVersion) {
    Path path = lockDir.file(getJavaName(javaVersion) + ".lock").get().getAsFile().toPath();
    return path;
  }

  private static String getJavaName(int version) {
    return "java" + version;
  }

  private static int getCurrentJavaVersion() {
    String versionString = System.getProperty("java.version");
    Matcher matcher = JAVA_MAJOR_VERSION.matcher(versionString);
    if (matcher.find()) {
      return Integer.parseInt(matcher.group(1));
    } else {
      throw new IllegalStateException("Unparseable java version: " + versionString);
    }
  }
}
