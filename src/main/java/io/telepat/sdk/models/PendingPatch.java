package io.telepat.sdk.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrei Marinescu on 19.06.2015.
 * JSON Patch container
 */

public class PendingPatch {
    private PatchType op;
    private String path;
    private Object value;
    private int objectId;

    public enum PatchType {
        add ("add"),
        replace ("replace"),
        increment ("increment"),
        delete ("delete");

        private final String name;

        private PatchType(String s) {
            name = s;
        }

        public boolean equalsName(String otherName){
            return (otherName != null) && name.equals(otherName);
        }

        public String toString(){
            return name;
        }
    }

    public PendingPatch(PatchType op, String path, Object value, int objectId) {
        this.op = op;
        this.path = path;
        this.value = value;
    }

    public PatchType getOp() {
        return op;
    }

    public void setOp(PatchType op) {
        this.op = op;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> patch = new HashMap<>();
        patch.put("op", op);
        patch.put("path", path);
        patch.put("value", value);
        return patch;
    }
}
