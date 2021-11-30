package com.lagou.pay.controller;


import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lagou.entity.Result;
import com.lagou.feign.SpuFeign;
import com.lagou.order.feign.OrderFeign;
import com.lagou.order.pojo.Order;
import com.lagou.pay.util.MatrixToImageWriter;
import com.lagou.pojo.Spu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

@RestController
@RequestMapping("/alipay")
public class AlipayController {

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private SpuFeign spuFeign;


    /**
     * 为了保持接口的幂等性，在前端系统调用该接口之前需要先进行支付状态的校验
     * 该接口申请二维码之前先判断支付状态
     * 请求二维码
     * @param orderId
     * @param response
     */
    @RequestMapping("/qrCode")
    public void preCreate(@RequestParam String orderId, HttpServletResponse response) throws Exception{
        // 1.获得订单对象，判断支付状态
        Order order = orderFeign.findById(orderId).getData();
        if (order == null) {
            response.getOutputStream().print("订单" + orderId + "不存在！");
            return;
        }
        if ("1".equals(order.getPayStatus())) {
            response.getOutputStream().print("订单" + orderId + "已支付！");
        }
        // 2.创建AlipayTradePrecreateRequest对象
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        // 3.创建AlipayTradePrecreateModel
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        // 3.1 设置商户订单号
        model.setOutTradeNo(orderId);
        // 3.2 卖家支付宝用户id
        model.setSellerId("2088621957204291");
        // 3.3 设置支付金额
        model.setTotalAmount(order.getTotalMoney().toString());
        // 3.4 设置商品标题/主题等
        model.setSubject("订单支付！");
        // model放入请求中
        request.setBizModel(model);

        // 4.发出请求，获得二维码连接
        AlipayTradePrecreateResponse atrResponse = alipayClient.execute(request);
        if (atrResponse.isSuccess() && "10000".equals(atrResponse.getCode())) {
            // 5.通过二维码链接生成收款二维码
            // 获得二维码
            String qrCode = atrResponse.getQrCode();
            // 绘制二维码
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bt = writer.encode(qrCode, BarcodeFormat.QR_CODE, 300, 300);
            // 生成二维码，写到输出流，返回到页面
            MatrixToImageWriter.writeToStream(bt, "jpg", response.getOutputStream());
            File file = new File("D:\\source\\lagou_shop_parent\\lagou_service\\lagou_service_pay\\src\\main\\resources\\qrcodes", orderId + ".jpg");
            MatrixToImageWriter.writeToFile(bt, "jpg", file);
        }

    }

}
