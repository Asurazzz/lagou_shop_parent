package com.lagou.pay.controller;


import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lagou.entity.Result;
import com.lagou.entity.StatusCode;
import com.lagou.feign.SpuFeign;
import com.lagou.order.feign.OrderFeign;
import com.lagou.order.pojo.Order;
import com.lagou.pay.config.AlipayConfig;
import com.lagou.pay.util.MatrixToImageWriter;
import com.lagou.pojo.Spu;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/alipay")
public class AlipayController {

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private AlipayClient alipayClient;

    @Autowired private AlipayConfig alipayConfig;

    @Autowired private RabbitMessagingTemplate rabbitTemplate;

    /**
     * 为了保持接口的幂等性，在前端系统调用该接口之前需要先进行支付状态的校验
     * 该接口申请二维码之前先判断支付状态
     * 请求二维码
     *
     * @param orderId
     * @param response
     */
    @RequestMapping("/qrCode")
    public void preCreate(@RequestParam String orderId, HttpServletResponse response) throws Exception {
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


    @GetMapping("/queryStatus")
    public String query(@RequestParam String outTradeNo) throws AlipayApiException {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" + "\"out_trade_no\":\""
                + outTradeNo + "\","
                + "\"org_pid\":\"2088621955637013\","
                + " \"query_options\":["
                + " \"trade_settle_info\"" + " ]" + " }");
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        String code = response.getCode();
        if (response.isSuccess() && "10000".equals(code)) {
            return response.getBody();
        } else {
            String subCode = response.getSubCode();
            if ("ACQ.SYSTEM_ERROR".equals(subCode)) {
                return "系统错误,请重新发起请求";
            }
            if ("ACQ.INVALID_PARAMETER".equals(subCode)) {
                return "参数无效,检查请求参数，修改后重新发起请 求";
            }
            if ("ACQ.TRADE_NOT_EXIST".equals(subCode)) {
                return "查询的交易不存在,检查传入的交易号是否正 确,请修改后重新发起请求";
            }
        }
        return response.getBody();
    }


    /**
     * 支付宝服务器异步通知URL
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/notify")
    public String notifyUrl(HttpServletRequest request) throws Exception {
        //一、获取并转换支付宝请求中参数
        Map<String, String> params = parseAlipayResultToMap(request);
        //二、验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipay_public_key(),
                alipayConfig.getCharset(), alipayConfig.getSigntype()); //调用SDK验证签名
        //签名验证成功 & 用户已经成功支付
        if (signVerified && "TRADE_SUCCESS".equals(params.get("trade_status"))) {
            //三、将数据发送MQ
            String message = prepareMQData(params);
            String exchange = params.get("body");
            rabbitTemplate.convertAndSend(exchange, "", message);
            return "success";
        } else {
            return "fail";
        }
    }


    /**
     * 关闭支付宝服务器的交易
     *
     * @param orderId
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping("/close")
    public Result close(@RequestParam String orderId) throws AlipayApiException {
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(orderId);
        request.setBizModel(model);
        AlipayTradeCloseResponse response = alipayClient.execute(request);
        if (response.isSuccess() && "10000".equals(response.getCode())) {
            return new Result(true, StatusCode.OK, "操作成功");
        } else {
            return new Result(false, StatusCode.ERROR, "操作失败");
        }
    }

    /**
     * 将阿里服务器请求中的数据转换为Map
     *
     * @param request
     * @return
     */
    private Map<String, String> parseAlipayResultToMap(HttpServletRequest request) {
        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        //支付宝请求中的参数
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        return params;
    }

    /**
     * 准备要发送到MQ中的数据
     *
     * @param params
     * @return
     */
    private String prepareMQData(Map<String, String> params) {
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("out_trade_no", params.get("out_trade_no"));
        messageMap.put("trade_no", params.get("trade_no"));
        messageMap.put("total_amount", params.get("total_amount"));
        //  yyyy-MM-dd HH:mm:ss
        messageMap.put("gmt_payment", params.get("gmt_payment"));
        //发动到MQ中
        return JSON.toJSONString(messageMap);
    }
}
