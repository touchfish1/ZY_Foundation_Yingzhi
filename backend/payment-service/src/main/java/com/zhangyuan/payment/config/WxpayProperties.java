package com.zhangyuan.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment.wxpay")
public class WxpayProperties {

    private boolean enabled;
    private String appId;
    private String mchId;
    private String apiV3Key;
    private String mchSerialNo;
    private String privateKeyPath;
    private String notifyUrl;
    private String nativeNotifyUrl;
    private String refundUrl;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getMchId() { return mchId; }
    public void setMchId(String mchId) { this.mchId = mchId; }
    public String getApiV3Key() { return apiV3Key; }
    public void setApiV3Key(String apiV3Key) { this.apiV3Key = apiV3Key; }
    public String getMchSerialNo() { return mchSerialNo; }
    public void setMchSerialNo(String mchSerialNo) { this.mchSerialNo = mchSerialNo; }
    public String getPrivateKeyPath() { return privateKeyPath; }
    public void setPrivateKeyPath(String privateKeyPath) { this.privateKeyPath = privateKeyPath; }
    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
    public String getNativeNotifyUrl() { return nativeNotifyUrl; }
    public void setNativeNotifyUrl(String nativeNotifyUrl) { this.nativeNotifyUrl = nativeNotifyUrl; }
    public String getRefundUrl() { return refundUrl; }
    public void setRefundUrl(String refundUrl) { this.refundUrl = refundUrl; }
}
