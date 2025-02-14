package org.jenkinsci.plugins.vmanager;

public class JUnitRequestHolder {

    private boolean generateJUnitXML;
    private boolean extraAttributesForFailures;
    private String staticAttributeList;
    private boolean noAppendSeed;

    public JUnitRequestHolder(
            boolean generateJUnitXML,
            boolean extraAttributesForFailures,
            String staticAttributeList,
            boolean noAppendSeed) {
        super();
        this.generateJUnitXML = generateJUnitXML;
        this.extraAttributesForFailures = extraAttributesForFailures;
        this.staticAttributeList = staticAttributeList;
        this.noAppendSeed = noAppendSeed;
    }

    public boolean isNoAppendSeed() {
        return noAppendSeed;
    }

    public void setNoAppendSeed(boolean noAppendSeed) {
        this.noAppendSeed = noAppendSeed;
    }

    public boolean isGenerateJUnitXML() {
        return generateJUnitXML;
    }

    public void setGenerateJUnitXML(boolean generateJUnitXML) {
        this.generateJUnitXML = generateJUnitXML;
    }

    public boolean istExtraAttributesForFailures() {
        return extraAttributesForFailures;
    }

    public void setExtraAttributesForFailuresType(boolean extraAttributesForFailures) {
        this.extraAttributesForFailures = extraAttributesForFailures;
    }

    public String getStaticAttributeList() {
        return staticAttributeList;
    }

    public void setStaticAttributeList(String staticAttributeList) {
        this.staticAttributeList = staticAttributeList;
    }
}
