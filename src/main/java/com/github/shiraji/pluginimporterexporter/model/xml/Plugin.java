package com.github.shiraji.pluginimporterexporter.model.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "plugin")
@XmlAccessorType(XmlAccessType.FIELD)
public class Plugin {
    @XmlAttribute
    private String id;
    @XmlAttribute
    private String url;
    @XmlAttribute
    private String version;
    private String name;
    @XmlJavaTypeAdapter(CDATAAdapter.class)
    private String description;
    @XmlJavaTypeAdapter(CDATAAdapter.class)
    @XmlElement(name = "change-notes")
    private String changeNotes;
    @XmlElement(name = "idea-version")
    private IdeaVersion ideaVersion;

    public Plugin() {
    }

    public Plugin(String id, String url, String version, String name) {
        this.id = id;
        this.url = url;
        this.version = version;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChangeNotes() {
        return changeNotes;
    }

    public void setChangeNotes(String changeNotes) {
        this.changeNotes = changeNotes;
    }

    public void setIdeaVersion(IdeaVersion ideaVersion) {
        this.ideaVersion = ideaVersion;
    }

    public IdeaVersion getIdeaVersion() {
        return ideaVersion;
    }
}
