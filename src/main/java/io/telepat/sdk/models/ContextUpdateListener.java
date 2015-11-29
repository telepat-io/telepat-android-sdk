package io.telepat.sdk.models;

/**
 * Created by Andrei Marinescu on 11/27/15.
 */
public interface ContextUpdateListener {
    void contextAdded(TelepatContext ctx);
    void contextUpdated(TelepatContext ctx);
    void contextEnded(TelepatContext ctx);
}
