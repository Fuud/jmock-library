package org.jmock.lib.action;

import org.jmock.api.Action;

import java.util.Collection;

public final class Actions {
    private Actions() {
    }

    public static Action checkIsEDT(){
        return new CheckIsEDTAction();
    }

    public static Action checkNotEDT(){
        return new CheckNotEDTAction();
    }

    public static Action doAll(Action... actions){
        return new DoAllAction(actions);
    }

    public static Action returnEnumeration(Collection collection){
        return new ReturnEnumerationAction(collection);
    }

    public static Action returnEnumeration(Object... values){
        return new ReturnEnumerationAction(values);
    }

    public static Action returnIterator(Collection collection){
        return new ReturnIteratorAction(collection);
    }

    public static Action returnIterator(Object... values){
        return new ReturnIteratorAction(values);
    }

    public static Action returnSet(Collection collection){
        return new ReturnSetAction(collection);
    }

    public static Action returnSet(Object... values){
        return new ReturnIteratorAction(values);
    }

    public static Action returnList(Collection collection){
        return new ReturnListAction(collection);
    }

    public static Action returnList(Object... values){
        return new ReturnIteratorAction(values);
    }

    public static Action sleep(long milliseconds){
        return new SleepAction(milliseconds);
    }

    public static Action throwException(Throwable exception){
        return new ThrowAction(exception);
    }
}
