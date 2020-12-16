package com.coofee.ttsdkbug;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class UncaughtExceptionHandlerStats {
    public static String TAG = "UncaughtExceptionHandlerStats";

    private static final long delayMillis = 4000L;

    private static final Handler sTimerTaskHandler = new Handler();

    private static final Runnable sTask = new Runnable() {
        private int counter = 0;

        @Override
        public void run() {
            Log.e(TAG, "timer#" + (counter++) + " root=" + dump());
            sTimerTaskHandler.postDelayed(this, delayMillis);
        }
    };

    public static void startTimerTask() {
        sTimerTaskHandler.postDelayed(sTask, delayMillis);
    }

    public static Node dump() {
        Log.e(TAG, "dump start.");
        Node root = new Node(Thread.getDefaultUncaughtExceptionHandler());
        HashSet<Thread.UncaughtExceptionHandler> handlerSet = new HashSet<>();
        dump(root, handlerSet);
        Log.e(TAG, "dump end.");
        return root;
    }

    private static void dump(@NonNull Node node, HashSet<Thread.UncaughtExceptionHandler> handlerSet) {
        if (node == null || node.uncaughtExceptionHandler == null) {
            return;
        }

        Log.e(TAG, "uncaughtExceptionHandler=" + node.uncaughtExceptionHandler);
        if (handlerSet.contains(node.uncaughtExceptionHandler)) {
            handlerSet.add(node.uncaughtExceptionHandler);
            Log.e(TAG, "find circular reference=" + node.uncaughtExceptionHandler +
                    ", handlerSet=" + handlerSet + ", just return.");
            return;
        }

        handlerSet.add(node.uncaughtExceptionHandler);

        Field[] declaredFields = node.uncaughtExceptionHandler.getClass().getDeclaredFields();
        if (declaredFields == null || declaredFields.length < 1) {
            return;
        }

        for (Field f : declaredFields) {
            try {
                f.setAccessible(true);
                Object fieldValue = f.get(node.uncaughtExceptionHandler);

                if (fieldValue instanceof Thread.UncaughtExceptionHandler
                        && fieldValue != node.uncaughtExceptionHandler) {
                    // avoid references circle com.networkbench.agent.impl.crash.d$a
                    Node child = new Node((Thread.UncaughtExceptionHandler) fieldValue);
                    node.addChild(child);
                    dump(child, handlerSet);
                } else {
                    if (fieldValue instanceof Collection) {
                        Log.e(TAG, "uncaughtExceptionHandler=" + node.uncaughtExceptionHandler + ", filed=" + f.getName() + " is collection=" + f.getType());

                        Type genericType = f.getGenericType();
                        if (genericType instanceof ParameterizedType) {
                            ParameterizedType parameterizedType = (ParameterizedType) genericType;
                            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                            Log.e(TAG, "parameterizedType=" + parameterizedType + ", actualTypeArguments=" + Arrays.toString(actualTypeArguments));

                            if (actualTypeArguments != null
                                    && actualTypeArguments.length > 0
                                    && Thread.UncaughtExceptionHandler.class == actualTypeArguments[0]) {
                                Collection<Thread.UncaughtExceptionHandler> uncaughtExceptionHandlers = new ArrayList<>((Collection<Thread.UncaughtExceptionHandler>) fieldValue);
                                Log.e(TAG, "uncaughtExceptionHandlers=" + uncaughtExceptionHandlers);

                                for (Thread.UncaughtExceptionHandler handler : uncaughtExceptionHandlers) {
                                    Node child = new Node(handler);
                                    node.addChild(child);
                                    dump(child, handlerSet);
                                }
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                Log.e(TAG, "error", e);
            }
        }
    }

    public static class Node {
        public final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
        public final List<Node> children = new ArrayList<>();

        public Node(@NonNull Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
            this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        }

        public void addChild(@NonNull Node child) {
            if (child != null) {
                children.add(child);
            }
        }

        @Override
        public String toString() {
            return "Node{" +
                    "uncaughtExceptionHandler=" + uncaughtExceptionHandler +
                    ", children=" + children +
                    '}';
        }
    }
}
