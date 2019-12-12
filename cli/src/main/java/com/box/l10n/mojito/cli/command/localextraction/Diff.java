package com.box.l10n.mojito.cli.command.localextraction;

import java.util.Set;

class Diff {
    Set<String> addedFiles;
    Set<String> removedFiles;
    Set<LocalTextUnitWithAssetPath> addedTextUnits;
    Set<LocalTextUnitWithAssetPath> removedTextUnits;

    public Set<String> getAddedFiles() {
        return addedFiles;
    }

    public void setAddedFiles(Set<String> addedFiles) {
        this.addedFiles = addedFiles;
    }

    public Set<String> getRemovedFiles() {
        return removedFiles;
    }

    public void setRemovedFiles(Set<String> removedFiles) {
        this.removedFiles = removedFiles;
    }

    public Set<LocalTextUnitWithAssetPath> getAddedTextUnits() {
        return addedTextUnits;
    }

    public void setAddedTextUnits(Set<LocalTextUnitWithAssetPath> addedTextUnits) {
        this.addedTextUnits = addedTextUnits;
    }

    public Set<LocalTextUnitWithAssetPath> getRemovedTextUnits() {
        return removedTextUnits;
    }

    public void setRemovedTextUnits(Set<LocalTextUnitWithAssetPath> removedTextUnits) {
        this.removedTextUnits = removedTextUnits;
    }
}
