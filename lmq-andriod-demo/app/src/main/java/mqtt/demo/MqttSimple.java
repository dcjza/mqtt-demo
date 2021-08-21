package mqtt.demo;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.util.Log;

public class MqttSimple {

    private MqttAndroidClient mqttAndroidClient;

    public MqttSimple(MqttAndroidClient mqttAndroidClient) {
        this.mqttAndroidClient = mqttAndroidClient;
    }

    public void test() {
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                //when connect success ,need resub
                Log.w("connectComplete", "connectComplete 当连接成功需要再次订阅" + reconnect + "    " + serverURI);
                subscribeToTopic();
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.e("close", "连接丢失了connectionLost", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String body = new String(message.getPayload());
                Log.w("消息到达messageArrived", body + "     来自于" + topic);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.w("deliveryComplete", "deliveryComplete" + token);
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setConnectionTimeout(3000);
        mqttConnectOptions.setKeepAliveInterval(90);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);

        try {
            // 参考 https://help.aliyun.com/document_detail/54225.html

            // Signature 方式
            mqttConnectOptions.setUserName("Signature|" + Config.accessKey + "|" + Config.instanceId);
            mqttConnectOptions.setPassword(Tool.macSignature(Config.clientId, Config.secretKey).toCharArray());

            Log.e("TEST", mqttConnectOptions.getUserName() + "     " + String.valueOf(mqttConnectOptions.getPassword()));

            /**
             * Token方式
             *  mqttConnectOptions.setUserName("Token|" + Config.accessKey + "|" + Config.instanceId);
             *  mqttConnectOptions.setPassword("RW|xxx");
             */
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("exception", "setPassword", e);
        }

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("connect", "连接成功去订阅onSuccess");
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                    Log.e("connect", "onFailure", exception);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e("connect", "exception", e);
        }
    }

    public void subscribeToTopic() {
        try {
            final String topicFilter[] = {Config.topic};
            final int[] qos = {1};
            mqttAndroidClient.subscribe(topicFilter, qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("subscribe", "订阅topic success，去发送消息");
                    publishMessage();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("subscribe", "订阅topic failed", exception);
                }
            });

        } catch (MqttException ex) {
            ex.printStackTrace();
            Log.e("subscribe", "exception", ex);
        }
    }


    public void publishMessage() {
        final String msg = "msg_test这是一条来自android11的消息。。。。来自于空的发送方法" + System.currentTimeMillis();
        publishMessage(msg);
    }

    public void publishMessage(final String msg) {
        try {
            MqttMessage message = new MqttMessage();

            message.setPayload(msg.getBytes());
            mqttAndroidClient.publish(Config.topic, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("publish", "发送成功success:" + msg);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("publish", "发送失败failed:" + msg);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e("publish", "exception", e);
        }
    }

}
