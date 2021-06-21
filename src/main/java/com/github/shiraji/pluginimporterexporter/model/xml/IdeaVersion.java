package com.github.shiraji.pluginimporterexporter.model.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "idea-version")
@XmlAccessorType(XmlAccessType.FIELD)
public class IdeaVersion {
    @XmlAttribute(name = "since-build")
    private String sinceBuild;
    @XmlAttribute(name = "until-build")
    private String untilBuild;

    public IdeaVersion() {
    }

    public IdeaVersion(String sinceBuild, String untilBuild) {
        this.sinceBuild = sinceBuild;
        this.untilBuild = untilBuild;
    }

    public String getSinceBuild() {
        return sinceBuild;
    }

    public void setSinceBuild(String sinceBuild) {
        this.sinceBuild = sinceBuild;
    }

    public String getUntilBuild() {
        return untilBuild;
    }

    public void setUntilBuild(String untilBuild) {
        this.untilBuild = untilBuild;
    }
}
