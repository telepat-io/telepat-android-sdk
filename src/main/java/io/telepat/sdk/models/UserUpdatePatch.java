package io.telepat.sdk.models;

/**
 * Created by Andrei Marinescu on 11/29/15.
 */
public class UserUpdatePatch {
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    private String fieldName;
    private String fieldValue;
}
