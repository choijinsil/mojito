package com.box.l10n.mojito.cli.command.localextraction;

import com.box.l10n.mojito.okapi.steps.AbstractMd5ComputationStep;
import net.sf.okapi.common.Event;

import java.util.ArrayList;
import java.util.List;

class ToLocalTextUnitsStep extends AbstractMd5ComputationStep {

    List<LocalTextUnit> localTextUnits;

    @Override
    protected Event handleStartDocument(Event event) {
        localTextUnits = new ArrayList<>();
        return super.handleStartDocument(event);
    }

    @Override
    public String getName() {
        return "Local text unit step";
    }

    @Override
    public String getDescription() {
        return "Convert text units into local text units";
    }

    @Override
    protected Event handleTextUnit(Event event) {
        Event eventToReturn = super.handleTextUnit(event);
        LocalTextUnit localTextUnit = new LocalTextUnit();
        localTextUnit.setName(name);
        localTextUnit.setSource(source);
        localTextUnit.setComments(comments);
        localTextUnits.add(localTextUnit);
        return eventToReturn;
    }

    public List<LocalTextUnit> getLocalTextUnits() {
        return localTextUnits;
    }
}
