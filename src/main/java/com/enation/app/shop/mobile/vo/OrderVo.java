package com.enation.app.shop.mobile.vo;

/**
 * Author: Dawei
 * Datetime: 2016-08-04 17:03
 */
public class OrderVo {

    /**
     * 配送方式id
     */
    private Integer shippingId;

    /**
     * 支付方式id
     */
    private Integer paymentId;

    /**
     * 收货地址id
     */
    private Integer addressId;

    /**
     * 配送时间
     */
    private String shippingTime;

    /**
     * 订单备注
     */
    private String remark;
    /**
     * 身份证信息
     */
    private String id_number;


    public String getId_number() {
		return id_number;
	}

	public void setId_number(String id_number) {
		this.id_number = id_number;
	}

	public Integer getShippingId() {
        return shippingId;
    }

    public void setShippingId(Integer shippingId) {
        this.shippingId = shippingId;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public String getShippingTime() {
        return shippingTime;
    }

    public void setShippingTime(String shippingTime) {
        this.shippingTime = shippingTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
