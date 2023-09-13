package cn.nfsn.transaction.bridge;

import cn.nfsn.transaction.config.WxPayConfig;
import cn.nfsn.transaction.enums.PayType;
import cn.nfsn.transaction.enums.WxApiType;
import cn.nfsn.transaction.enums.WxNotifyType;
import cn.nfsn.transaction.model.dto.ProductDTO;
import cn.nfsn.transaction.model.entity.OrderInfo;
import cn.nfsn.transaction.service.OrderInfoService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: WxPayStrategy
 * @Description: 微信支付策略
 * @Author: atnibamaitay
 * @CreateTime: 2023/9/8 0008 13:33
 **/
@Slf4j
@Component
public class WxPayNative implements IPayMode {
    private final Gson gson = new Gson();

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private WxPayConfig wxPayConfig;

    @Resource
    private CloseableHttpClient wxPayClient;

    /**
     * 创建订单，调用Native支付接口
     *
     * @param productDTO 商品信息
     * @return 包含code_url 和 订单号的Map
     * @throws Exception 抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(ProductDTO productDTO) throws Exception {
        // 打印日志，开始生成订单
        log.info("开始生成订单");
        System.out.println(productDTO);

        //根据商品ID和支付类型创建订单
        OrderInfo orderInfo = orderInfoService.createOrderByProductId(productDTO, PayType.WXPAY.getType());

        //获取订单二维码URL
        String codeUrl = orderInfo.getCodeUrl();

        //检查订单是否存在且二维码URL是否已保存
        if(orderInfo != null && !StringUtils.isEmpty(codeUrl)){
            // 添加订单号到日志
            log.info("订单：{} 已存在，二维码已保存", orderInfo.getOrderNo());

            //初始化返回结果映射
            Map<String, Object> map = new HashMap<>();
            map.put("codeUrl", codeUrl);
            map.put("orderNo", orderInfo.getOrderNo());

            //返回二维码和订单号
            return map;
        }

        //打印日志，开始调用统一下单API
        log.info("订单：{} 开始调用统一下单API", orderInfo.getOrderNo());

        //创建POST请求
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType()));

        //设置请求参数
        Map paramsMap = new HashMap();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        paramsMap.put("description", orderInfo.getTitle());
        paramsMap.put("out_trade_no", orderInfo.getOrderNo());
        //设置支付通知的API接口
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));

        //设置金额参数
        Map amountMap = new HashMap();
        amountMap.put("total", orderInfo.getTotalFee());
        amountMap.put("currency", "CNY");
        paramsMap.put("amount", amountMap);

        //将请求参数转换为JSON格式
        String jsonParams = gson.toJson(paramsMap);

        //打印请求参数信息
        log.info("订单：{} 请求参数 ===> {}", orderInfo.getOrderNo(), jsonParams);

        //设置请求实体
        StringEntity entity = new StringEntity(jsonParams,"utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        CloseableHttpResponse response = null;
        try {
            //发送请求并获取响应
            response = wxPayClient.execute(httpPost);

            //获取响应内容和状态码
            //响应体
            String bodyAsString = EntityUtils.toString(response.getEntity());
            //响应状态码
            int statusCode = response.getStatusLine().getStatusCode();

            //根据状态码处理响应结果
            if (statusCode == 200) {
                // 添加订单号到日志
                log.info("订单：{} 成功, 返回结果 = {}", orderInfo.getOrderNo(), bodyAsString);
            } else if (statusCode == 204) {
                // 添加订单号到日志
                log.info("订单：{} 成功", orderInfo.getOrderNo());
            } else {
                // 添加订单号到日志
                log.error("订单：{} Native下单失败,响应码 = {},返回结果 = {}", orderInfo.getOrderNo(), statusCode, bodyAsString);
                throw new IOException("request failed");
            }

            //解析响应结果
            Map<String, String> resultMap = gson.fromJson(bodyAsString, HashMap.class);

            //从结果中获取二维码URL
            codeUrl = resultMap.get("code_url");

            //保存二维码URL
            String orderNo = orderInfo.getOrderNo();
            orderInfoService.saveCodeUrl(orderNo, codeUrl);

            //初始化返回结果映射
            Map<String, Object> map = new HashMap<>();
            map.put("codeUrl", codeUrl);
            map.put("orderNo", orderInfo.getOrderNo());

            //返回二维码和订单号
            return map;

        } finally {
            //关闭响应，先检查response是否为null
            if(response != null){
                response.close();
            }
        }
    }

}