package io.github.Hatsuduki;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.util.ExecUtil;

import java.io.IOException;

public class shutdown {
    public static void main(String[] args) throws InterruptedException {
        final GpioController gpio = GpioFactory.getInstance();
        final GpioPinDigitalInput pin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN);//シャットダウン用pin
        final GpioPinDigitalOutput errorLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02);//LED用pin
        pin.setShutdownOptions(true);
        errorLed.setShutdownOptions(true,PinState.LOW);

        pin.addListener(new GpioPinListenerDigital() {//pinにevent追加
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                if(event.getEdge()==PinEdge.RISING){//lowからhighになったとき
                    int count = 0;
                    while(true){
                        if(pin.isHigh()){//スイッチが押されたとき
                            count++;
                            if(count>=15) {//0.2秒*15回分
                                try {
                                    ExecUtil.execute("sudo shutdown -h now");//シャットダウンコマンド
                                } catch (IOException | InterruptedException e) {//エラー処理
                                    //e.printStackTrace();
                                    errorLed.blink(250,2000);//エラーが起きたらLEDを点滅させる
                                    errorLed.low();
                                    break;
                                }
                                break;
                            }
                        }else{
                            break;
                        }
                        try {
                            Thread.sleep(200);//0.2秒待つ
                        } catch (InterruptedException e) {//エラー処理
                            //e.printStackTrace();
                            errorLed.blink(250,2000);//エラーが起きたらLEDを点滅させる
                            errorLed.low();
                            break;
                        }
                    }
                }
            }
        });

        while(true){
            Thread.sleep(100);
        }
    }
}
