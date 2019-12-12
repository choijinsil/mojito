package com.box.l10n.mojito.cli.command.localextraction;

import java.util.List;

public class LocalExtraction {

    List<LocalTextUnit> localTextUnits;

    List<String> filterOptions;

    String fileType;

    String name;

    public List<LocalTextUnit> getLocalTextUnits() {
        return localTextUnits;
    }

    public void setLocalTextUnits(List<LocalTextUnit> localTextUnits) {
        this.localTextUnits = localTextUnits;
    }

    public List<String> getFilterOptions() {
        return filterOptions;
    }

    public void setFilterOptions(List<String> filterOptions) {
        this.filterOptions = filterOptions;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
