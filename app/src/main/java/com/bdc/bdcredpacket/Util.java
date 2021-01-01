package com.bdc.bdcredpacket;

import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.reflect.Field;

public class Util {
    public static long getNodeSourceId(AccessibilityNodeInfo nodeInfo){
        try {
            Field fieldSourceId = nodeInfo.getClass().getDeclaredField("mSourceNodeId");
            fieldSourceId.setAccessible(true);
            Object o = fieldSourceId.get(nodeInfo);
            if(o!=null){
                return (Long)o;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;

    }

    public static long getNodeLabelId(AccessibilityNodeInfo nodeInfo){
        try {
            Field fieldSourceId = nodeInfo.getClass().getDeclaredField("mLabeledById");
            fieldSourceId.setAccessible(true);
            Object o = fieldSourceId.get(nodeInfo);
            if(o!=null){
                return (Long)o;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;

    }


}
