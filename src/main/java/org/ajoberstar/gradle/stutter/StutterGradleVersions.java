package org.ajoberstar.gradle.stutter;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.util.GradleVersion;

public class StutterGradleVersions {
  private Set<GradleVersion> compatibleVersions = new HashSet<>();
  private Set<GradleVersion> incompatibleVersions = new HashSet<>();
  private List<Predicate<GradleVersion>> compatibleRanges = new ArrayList<>();

  public void compatibleRange(String startingInclusive) {
    var start = GradleVersion.version(startingInclusive);
    compatibleRanges.add(version -> version.getBaseVersion().compareTo(start) >= 0);
  }

  public void compatibleRange(String startingInclusive, String endingExclusive) {
    var start = GradleVersion.version(startingInclusive);
    var end = GradleVersion.version(endingExclusive);

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

  Stream<GradleVersion> resolve(Set<GradleVersion> versions, boolean sparse) {
    var knownCompatibleExact = versions.stream()
        .filter(compatibleVersions::contains);

    var knownCompatibleRange = versions.stream()
        .filter(v -> compatibleRanges.stream().anyMatch(p -> p.test(v)));

    var knownCompatible = Stream.concat(knownCompatibleExact, knownCompatibleRange)
        .filter(version -> !incompatibleVersions.contains(version));

    var knownCompatibleMinors = compatibleGradleMinors(knownCompatible);

    if (sparse) {
      return compatibleGradleSparse(knownCompatibleMinors);
    } else {
      return knownCompatibleMinors;
    }
  }

  private Stream<GradleVersion> compatibleGradleMinors(Stream<GradleVersion> versions) {
    Function<GradleVersion, String> minorVersion = version -> {
      var parts = version.getBaseVersion().getVersion().split("\\.");
      if (parts.length < 2) {
        throw new IllegalArgumentException("Version doesn't contain a major and minor component: " + version);
      } else {
        return String.format("%s.%s", parts[0], parts[1]);
      }
    };

    // only include most mature version for each minor (i.e. no rcs if a final exists, no .0 if a .1
    // patch exists)
    Function<List<GradleVersion>, Stream<GradleVersion>> toMax = vers -> {
      var max = vers.stream().max(Comparator.naturalOrder());
      var maxFinal = vers.stream().filter(ver -> ver.equals(ver.getBaseVersion())).max(Comparator.naturalOrder());

      return Stream.of(max, maxFinal)
          .flatMap(Optional::stream);
    };

    var versionsByMinor = versions.collect(Collectors.groupingBy(minorVersion));
    return versionsByMinor.values().stream()
        .flatMap(toMax);
  }

  private Stream<GradleVersion> compatibleGradleSparse(Stream<GradleVersion> versions) {
    Function<GradleVersion, String> majorVersion = version -> version.getVersion().substring(0, version.getVersion().indexOf('.'));

    Function<List<GradleVersion>, Stream<GradleVersion>> toMinMax = vers -> {
      var min = vers.stream().min(Comparator.naturalOrder());
      var max = vers.stream().max(Comparator.naturalOrder());
      var maxFinal = vers.stream().filter(ver -> ver.equals(ver.getBaseVersion())).max(Comparator.naturalOrder());

      return Stream.of(min, max, maxFinal)
          .flatMap(Optional::stream);
    };

    var versionsByMajor = versions.collect(Collectors.groupingBy(majorVersion));
    return versionsByMajor.values().stream()
        .flatMap(toMinMax);
  }
}
