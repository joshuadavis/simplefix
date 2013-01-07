package org.simplefix.dictionary.xml;

/**
* Enum for the sections of a QFJ XML data dictionary.
* <br>
* User: josh
* Date: 1/7/13
* Time: 7:35 AM
*/
enum Section {
    HEADER("fix/header"),
    TRAILER("fix/trailer"),
    MESSAGES("fix/messages"),
    COMPONENTS("fix/components"),
    FIELDS("fix/fields")
    ;

    private String path;

    Section(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public static Section getSection(String path) {
        for (Section section : Section.values()) {
            if (section.getPath().equals(path)) {
                return section;
            }
        }
        return null;
    }
}
