package com.box.l10n.mojito.cli.command.localextraction;

import java.util.Comparator;

public class LocalTextUnitComparators {

    /**
     * Sort text units by filename, name, source and then comments
     */
    public static final Comparator<LocalTextUnitWithAssetPath> BY_FILENAME_NAME_SOURCE_COMMENTS = Comparator
            .comparing(LocalTextUnitWithAssetPath::getAssetPath, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(LocalTextUnitWithAssetPath::getName, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(LocalTextUnitWithAssetPath::getSource, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(LocalTextUnitWithAssetPath::getComments, Comparator.nullsFirst(Comparator.naturalOrder()));
}
