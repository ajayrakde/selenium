package org.openqa.selenium.locator.bidi.model;

public record BidiSnapshotBundle(
    NetworkSnapshot network,
    ConsoleSnapshot console,
    NavigationSnapshot navigation) {}
