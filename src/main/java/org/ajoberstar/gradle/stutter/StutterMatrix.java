package org.ajoberstar.gradle.stutter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.gradle.util.GradleVersion;

public class StutterMatrix {
  private final int javaVersion;
  private Set<GradleVersion> compatibleVersions = new HashSet<>();
  private Set<GradleVersion> incompatibleVersions = new HashSet<>();
  private List<Predicate<GradleVersion>> compatibleRanges = new ArrayList<>();

  @Inject
  public StutterMatrix(int javaVersion) {
    this.javaVersion = javaVersion;
  }

  public int getJavaVersion() {
    return javaVersion;
  }

  public void compatibleRange(String startingInclusive) {
    GradleVersion start = GradleVersion.version(startingInclusive);
    compatibleRanges.add(version -> version.getBaseVersion().compareTo(start) >= 0);
  }

  public void compatibleRange(String startingInclusive, String endingExclusive) {
    GradleVersion start = GradleVersion.version(startingInclusive);
    GradleVersion end = GradleVersion.version(endingExclusive);

    if (start.compareTo(end) >= 0) {
      throw new IllegalArgumentException("Starting version must be less than ending version: " + startingInclusive + " to " + endingExclusive);
    }
    compatibleRanges.add(version -> version.getBaseVersion().compareTo(start) >= 0 && version.getBaseVersion().compareTo(end) < 0);
  }

  public void incompatible(String... versions) {
    Arrays.stream(versions)
        .map(GradleVersion::version)
        .forEach(incompatibleVersions::add);
  }

  public void compatible(String... versions) {
    Arrays.stream(versions)
        .map(GradleVersion::version)
        .forEach(compatibleVersions::add);
  }

  Stream<GradleVersion> allCompatible(Set<GradleVersion> versions, boolean sparse) {
    Stream<GradleVersion> knownCompatibleExact = versions.stream()
        .filter(compatibleVersions::contains);

    Stream<GradleVersion> knownCompatibleRange = versions.stream()
        .filter(v -> compatibleRanges.stream().anyMatch(p -> p.test(v)));

    Stream<GradleVersion> knownCompatible = Stream.concat(knownCompatibleExact, knownCompatibleRange)
        .filter(version -> !incompatibleVersions.contains(version));

    Stream<GradleVersion> knownCompatibleMinors = compatibleMinors(knownCompatible);

    if (sparse) {
      return compatibleSparse(knownCompatibleMinors);
    } else {
      return knownCompatibleMinors;
    }
  }

  private Stream<GradleVersion> compatibleMinors(Stream<GradleVersion> versions) {
    Function<GradleVersion, String> minorVersion = version -> {
      String[] parts = version.getBaseVersion().getVersion().split("\\.");
      if (parts.length < 2) {
        throw new IllegalArgumentException("Version doesn't contain a major and minor component: " + version);
      } else {
        return String.format("%s.%s", parts[0], parts[1]);
      }
    };

    // only include most mature version for each minor (i.e. no rcs if a final exists, no .0 if a .1
    // patch exists)
    Function<List<GradleVersion>, Stream<GradleVersion>> toMax = vers -> {
      Optional<GradleVersion> max = vers.stream().max(Comparator.naturalOrder());
      Optional<GradleVersion> maxFinal = vers.stream().filter(ver -> ver.equals(ver.getBaseVersion())).max(Comparator.naturalOrder());

      return Stream.of(max, maxFinal)
          // TODO Java 9 Optional::stream
          .filter(Optional::isPresent)
          .map(Optional::get);
    };

    Map<String, List<GradleVersion>> versionsByMinor = versions.collect(Collectors.groupingBy(minorVersion));
    return versionsByMinor.values().stream()
        .flatMap(toMax);
  }

  private Stream<GradleVersion> compatibleSparse(Stream<GradleVersion> versions) {
    Function<GradleVersion, String> majorVersion = version -> version.getVersion().substring(0, version.getVersion().indexOf('.'));

    Function<List<GradleVersion>, Stream<GradleVersion>> toMinMax = vers -> {
      Optional<GradleVersion> min = vers.stream().min(Comparator.naturalOrder());
      Optional<GradleVersion> max = vers.stream().max(Comparator.naturalOrder());
      Optional<GradleVersion> maxFinal = vers.stream().filter(ver -> ver.equals(ver.getBaseVersion())).max(Comparator.naturalOrder());

      return Stream.of(min, max, maxFinal)
          // TODO Java 9 Optional::stream
          .filter(Optional::isPresent)
          .map(Optional::get);
    };

    Map<String, List<GradleVersion>> versionsByMajor = versions.collect(Collectors.groupingBy(majorVersion));
    return versionsByMajor.values().stream()
        .flatMap(toMinMax);
  }
}
