package mqtt.demo;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.paho.android.service.MqttAndroidClient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private MqttAndroidClient mqttAndroidClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), Config.serverUri, Config.clientId);
        final MqttSimple mqttSimple = new MqttSimple(mqttAndroidClient);
        mqttSimple.test();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                while (true) {
                    try {
                        Thread.sleep(5000);
                        mqttSimple.publishMessage("来自" + getClass().getName() + "消息发送" + sdf.format(new Date()) + "测试");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttAndroidClient.unregisterResources();
    }

}
