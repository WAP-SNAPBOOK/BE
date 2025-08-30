package com.example.easybooking.form.domain.type;


public enum FieldType {
    TEXT("text"),
    NUMBER("number"),
    RADIO("radio"),
    SELECT("select"),
    FILE("file"),
    TEXTAREA("textarea"),
    CHECKBOX("checkbox");

    private final String name;
    FieldType(String name) { this.name = name; }
}