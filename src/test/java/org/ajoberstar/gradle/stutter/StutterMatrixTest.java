package org.ajoberstar.gradle.stutter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.Test;

public class StutterMatrixTest {
  private final Set<GradleVersion> versions = Stream.of(
      "3.0", "3.1", "3.2", "3.2.1", "3.3", "3.3.1", "3.4", "3.5", "4.0-rc-2", "4.0", "4.1", "4.2.1", "4.3", "4.4-rc-1").map(GradleVersion::version).collect(Collectors.toSet());

  @Test
  public void onlySpecifyingCompatibleReturnsExactMatches() {
    StutterMatrix matrix = new StutterMatrix(11);
    matrix.compatible("3.2.1", "4.1");
    matrix.compatible("3.4");

    Set<String> expected = Stream.of("3.2.1", "3.4", "4.1")
        .collect(Collectors.toSet());

    Set<String> actual = matrix.allCompatible(versions, false)
        .map(GradleVersion::getVersion)
        .collect(Collectors.toSet());

    assertEquals(expected, actual, "compatible should only match exact versions");
  }

  @Test
  public void onlySpecifyingCompatibleRangeStartEndReturnsAllWithinRange() {
    StutterMatrix matrix = new StutterMatrix(11);
    matrix.compatibleRange("3.0", "4.0");

    Set<String> expected = Stream.of("3.0", "3.1", "3.2", "3.2.1", "3.3", "3.3.1", "3.4", "3.5")
        .collect(Collectors.toSet());

    Set<String> actual = matrix.allCompatible(versions, false)
        .map(GradleVersion::getVersion)
        .collect(Collectors.toSet());

    assertEquals(expected, actual, "compatibleRange should match all versions in range");
  }

  @Test
  public void onlySpecifyingCompatibleRangeStartReturnsAllWithinRange() {
    StutterMatrix matrix = new StutterMatrix(11);
    matrix.compatibleRange("4.0");

    Set<String> expected = Stream.of("4.0-rc-2", "4.0", "4.1", "4.2.1", "4.3", "4.4-rc-1")
        .collect(Collectors.toSet());

    Set<String> actual = matrix.allCompatible(versions, false)
        .map(GradleVersion::getVersion)
        .collect(Collectors.toSet());

    assertEquals(expected, actual, "compatibleRange should match all versions in range");
  }

  @Test
  public void usingIncompatibleWithCompatibleRangeExcludesVersionsOtherwiseWithinRange() {
    StutterMatrix matrix = new StutterMatrix(11);
    matrix.compatibleRange("4.0");
    matrix.incompatible("4.1");

    Set<String> expected = Stream.of("4.0-rc-2", "4.0", "4.2.1", "4.3", "4.4-rc-1")
        .collect(Collectors.toSet());

    Set<String> actual = matrix.allCompatible(versions, false)
        .map(GradleVersion::getVersion)
        .collect(Collectors.toSet());

    assertEquals(expected, actual, "compatibleRange should match all versions in range, except those marked incompatible");
  }

  @Test
  public void usingSparseMatchesMinMaxOtherwiseCompatibleFromEachMajor() {
    StutterMatrix matrix = new StutterMatrix(11);
    matrix.compatibleRange("3.0");
    matrix.compatible("3.2.1");

    Set<String> expected = Stream.of("3.0", "3.5", "4.0-rc-2", "4.4-rc-1")
        .collect(Collectors.toSet());

    Set<String> actual = matrix.allCompatible(versions, true)
        .map(GradleVersion::getVersion)
        .collect(Collectors.toSet());

    assertEquals(expected, actual, "sparse only matches the min/max version that otherwise matches in each major version");
  }
}
