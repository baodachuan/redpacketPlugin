
package com.bdc.bdcredpacket;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;


import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.view.accessibility.AccessibilityEvent.CONTENT_CHANGE_TYPE_SUBTREE;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "BDCAccessibilityService";
    /**
     * 微信几个页面的包名+地址。用于判断在哪个页面
     * LAUCHER-微信聊天界面
     * LUCKEY_MONEY_RECEIVER-点击红包弹出的界面
     * LUCKEY_MONEY_DETAIL-红包领取后的详情界面
     */
    private String LAUCHER = "com.tencent.mm.ui.LauncherUI";
    private String LUCKEY_MONEY_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    private String LUCKEY_MONEY_RECEIVER = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";

    private Map<AccessibilityNodeInfo, Boolean> pickedMap = new MaxSizeMap<>(20);

    /**
     * 用于判断是否点击过红包了
     */
    private boolean isOpenRP;

    String CHANNEL_ONE_ID = "CHANNEL_ONE_ID";
    String CHANNEL_ONE_NAME = "CHANNEL_ONE_ID";
    NotificationChannel notificationChannel = null;
    private long startTime;

    private Handler mHandler = new Handler();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e(TAG, "receive event " + event);
        int eventType = event.getEventType();
        switch (eventType) {
            //通知栏来信息，判断是否含有微信红包字样，是的话跳转
//            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
//                List<CharSequence> texts = event.getText();
//                for (CharSequence text : texts) {
//                    String content = text.toString();
//                    if (!TextUtils.isEmpty(content)) {
//                        //判断是否含有[微信红包]字样
//                        if (content.contains("[微信红包]")) {
//                            //如果有则打开微信红包页面
//                            openWeChatPage(event);
//                            isOpenRP = false;
//                        }
//                    }
//                }
//                break;
            //界面跳转的监听
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:

                String className = event.getClassName().toString();
                Log.e(TAG, "WINDOW_STATE_CHANGED" + className);
                //判断是否是显示‘开’的那个红包界面
                if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI".equals(className)) {
                    // 实测有概率rootNode为null，延时150后，不易为空
//                    SystemClock.sleep(150);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                            //开始抢红包
                            Log.e(TAG, "LuckeyMoneyUI, rootNode is " + rootNode);
                            openRedPacket(rootNode);
                        }
                    }, 180);

                } else if (LUCKEY_MONEY_DETAIL.equals(className)) {
                    performBackClick();
                }
                //内容变化的监听
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (event.getContentChangeTypes() == CONTENT_CHANGE_TYPE_SUBTREE) {
                    return;
                }
                //防止多次调用
                if (event.getEventTime() - startTime < 200) {
                    return;
                }
                startTime = event.getEventTime();
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                //开始找红包
                clickRedPacket(rootNode);
//                findRedPacket(rootNode);
                break;
        }

    }

    private void clickRedPacket(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            Log.e("bdc", "rootNode is null");
            return;
        }
        List<AccessibilityNodeInfo> nodes1 = findAccessibilityNodeInfosByTexts(rootNode, new String[]{
                "微信红包"});
        if (!nodes1.isEmpty()) {
            int size = nodes1.size();
            for (int i = size - 1; i > 0; i--) {
//                AccessibilityNodeInfo targetNode = nodes1.get(nodes1.size() - 1);
                AccessibilityNodeInfo targetNode = nodes1.get(i);
                Log.e(TAG, "targetNode text is " + targetNode);
//            long sourceId = Util.getNodeSourceId(targetNode);
//            long labelId = Util.getNodeLabelId(targetNode);
//            Log.e(TAG, "targetNode sourceId is," + sourceId+"label id,"+ labelId);
                while (targetNode != null) {
                    if (!targetNode.isClickable()) {
                        targetNode = targetNode.getParent();
                    } else {
                        break;
                    }
                }
                if (targetNode != null) {//避免被文字干扰导致外挂失效
//                Rect rectInScreen=new Rect();
//                targetNode.getBoundsInScreen(rectInScreen);
//                if (rectInScreen.top < 1800) {
//                    Log.e(TAG, " tartNode is old");
//                    return;
//                }
                    List<AccessibilityNodeInfo> alreadyReceivedNode = findAccessibilityNodeInfosByTexts(targetNode, new String[]{"已领取"});
                    if (!alreadyReceivedNode.isEmpty()) {
                        Log.e(TAG, "tartNode is 已领取");
                        continue;
                    }
                    List<AccessibilityNodeInfo> alreadyEmptyNode = findAccessibilityNodeInfosByTexts(targetNode, new String[]{"已被领完"});
                    if (!alreadyEmptyNode.isEmpty()) {
                        Log.e(TAG, "tartNode is 已领完");
                        continue;
                    }

                    Log.e(TAG, "real tartNode is" + targetNode);
                    targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return;
                } else {
                    Log.e(TAG, "this is text");
                }
            }
        }
    }

    public void performBackClick() {
//        try {
//            Thread.sleep(10);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        SystemClock.sleep(1000);
        Log.i(TAG, "performBackClick");
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    /**
     * 遍历查找红包
     */
    private void findRedPacket(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            //从最后一行开始找起
            for (int i = rootNode.getChildCount() - 1; i >= 0; i--) {
                AccessibilityNodeInfo node = rootNode.getChild(i);
                //如果node为空则跳过该节点
                if (node == null) {
                    continue;
                }
                CharSequence text = node.getText();
                if (text != null && text.toString().equals("微信红包")) {
                    AccessibilityNodeInfo parent = node.getParent();
                    //while循环,遍历"领取红包"的各个父布局，直至找到可点击的为止
                    while (parent != null) {
                        if (parent.isClickable()) {
                            //模拟点击
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            //isOpenRP用于判断该红包是否点击过
                            isOpenRP = true;

                            break;
                        }
                        parent = parent.getParent();
                    }
                }
                //判断是否已经打开过那个最新的红包了，是的话就跳出for循环，不是的话继续遍历
                if (isOpenRP) {
                    break;
                } else {
                    findRedPacket(node);
                }

            }
        }
    }

    private List<AccessibilityNodeInfo> findAccessibilityNodeInfosByTexts(AccessibilityNodeInfo nodeInfo, String[] texts) {
        for (String text : texts) {
            if (text == null) continue;
            List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(text);
            if (!nodes.isEmpty()) return nodes;
        }
        return new ArrayList<>();
    }

    /**
     * 开始打开红包
     */
    private void openRedPacket(final AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            Toast.makeText(this, "rootnode is null", Toast.LENGTH_SHORT).show();
            checkItLater();
            return;
        }
        Log.e(TAG, "start openRedPacket");
        boolean clicked = performOpenClick(rootNode);

        if (!clicked) {
            checkItLater();
        }
    }

    private void checkItLater(){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                    Toast.makeText(MyAccessibilityService.this, "再来一次", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "再来一次");
                AccessibilityNodeInfo rootNode2 = getRootInActiveWindow();
                openRedPacketByTraversal(rootNode2);
            }
        }, 200);
    }

    private boolean performOpenClick(AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> kaiNode = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/den");
        if (!kaiNode.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : kaiNode) {
                Log.e(TAG, "ID来了");
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            return true;
        }
        return false;
    }


    private void openRedPacketByTraversal(AccessibilityNodeInfo rootNode) {
        Log.e(TAG, "openRedPacketByTraversal");
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo node = rootNode.getChild(i);
            if ("android.widget.Button".equals(node.getClassName())) {
                Log.e(TAG, "来了");
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return;
            }
            openRedPacketByTraversal(node);
        }
    }


    /**
     * 开启红包所在的聊天页面
     */
    private void openWeChatPage(AccessibilityEvent event) {
        //A instanceof B 用来判断内存中实际对象A是不是B类型，常用于强制转换前的判断
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            //打开对应的聊天界面
            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 服务连接
     */
//    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onServiceConnected() {
        Toast.makeText(this, "抢红包服务开启", Toast.LENGTH_SHORT).show();
        super.onServiceConnected();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(notificationChannel);
        }

        Intent startIntent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        PendingIntent startPendingIntent = PendingIntent.getActivity(this, 111, startIntent, 0);

        Intent stopIntent = new Intent(this, MyAccessibilityService.class);
        stopIntent.putExtra("action_stop", true);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 222, stopIntent, 0);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        remoteViews.setOnClickPendingIntent(R.id.start, startPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.stop, stopPendingIntent);


        //开启前台服务
        // 参数一：唯一的通知标识；参数二：通知消息。
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notification = new Notification.Builder(this).setChannelId(CHANNEL_ONE_ID)
                    .setTicker("Nature")
                    .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle("不要关闭")
//                .setContentText("小助手")
                    .setCustomContentView(remoteViews)
                    .build();
            startForeground(1, notification);
        } else
        //发送一般通知
        {
            Notification notification = new Notification.Builder(this)
                    .setTicker("Nature")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("不要关闭")
                    .setContentText("小助手")
                    .build();
            notification.contentView = remoteViews;
            manager.notify(1, notification);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean stop = intent.getBooleanExtra("action_stop", false);
        if (stop) {
            disableSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 必须重写的方法：系统要中断此service返回的响应时会调用。在整个生命周期会被调用多次。
     */
    @Override
    public void onInterrupt() {
        Toast.makeText(this, "我快被终结了啊-----", Toast.LENGTH_SHORT).show();
    }

    /**
     * 服务断开
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "抢红包服务已被关闭", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }

}
