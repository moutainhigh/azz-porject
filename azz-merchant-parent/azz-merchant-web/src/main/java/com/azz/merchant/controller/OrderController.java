/*******************************************************************************
 * Project Key : CPPII Create on 2018年10月15日 下午3:05:46 Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/

package com.azz.merchant.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.azz.core.common.JsonResult;
import com.azz.core.common.page.Pagination;
import com.azz.merchant.utils.WebUtils;
import com.azz.order.api.merchant.MerchantOrderService;
import com.azz.order.merchant.pojo.bo.EditOrderStatus;
import com.azz.order.merchant.pojo.bo.EditOrderStatusWebParam;
import com.azz.order.merchant.pojo.bo.SearchOrderDetailParam;
import com.azz.order.merchant.pojo.bo.SearchOrderListParam;
import com.azz.order.merchant.pojo.bo.ShipmentFile;
import com.azz.order.merchant.pojo.vo.OrderDetail;
import com.azz.order.merchant.pojo.vo.OrderList;
import com.azz.util.Base64;

/**
 * <P>
 * 商户端订单管理
 * </P>
 * 
 * @version 1.0
 * @author 彭斌 2018年11月15日 下午2:38:45
 */
@RestController
@RequestMapping("/azz/api/merchant/order")
public class OrderController {


    @Autowired
    MerchantOrderService orderService;

    /**
     * <p>
     * 查询商户订单管理
     * </p>
     * 
     * @param param
     * @return
     * @author 彭斌 2018年11月15日 下午2:50:20
     */
    @RequestMapping("/getMerchantOrderList")
    public JsonResult<Pagination<OrderList>> getMerchantOrderList(SearchOrderListParam param) {
        return orderService.getMerchantOrderList(param);
    }

    /**
     * <p>
     * 获取商户订单详情
     * </p>
     * 
     * @param param
     * @return
     * @author 彭斌 2018年11月15日 下午2:50:27
     */
    @RequestMapping("/getMerchantOrderDetail")
    public JsonResult<OrderDetail> getMerchantOrderDetail(SearchOrderDetailParam param) {
        param.setMerchantId(WebUtils.getLoginMerchanUser().getMerchantUserInfo().getMerchantId());
        return orderService.getMerchantOrderDetail(param);
    }

    /**
     * <p>
     * 订单流转状态变更
     * </p>
     * 
     * @param param
     * @return
     * @author 彭斌 2018年11月15日 下午2:50:34
     */
    @RequestMapping("/editMerchantOrderStatus")
    JsonResult<String> editMerchantOrderStatus(EditOrderStatusWebParam param) throws IOException {
        param.setMerchantId(WebUtils.getLoginMerchanUser().getMerchantUserInfo().getMerchantId());
        param.setModifier(WebUtils.getLoginMerchanUser().getMerchantUserInfo().getMerchantCode());
        List<ShipmentFile> sf = null;
        if (param.getStatus() == 2) {
            sf = new ArrayList<>();
            List<MultipartFile> shipmentFiles = param.getShipmentFiles();
            for (int i = 0; i < shipmentFiles.size(); i++) {
                ShipmentFile shipmentFile = new ShipmentFile(
                        shipmentFiles.get(i).getOriginalFilename(), shipmentFiles.get(i).getSize(),
                        Base64.encode(shipmentFiles.get(i).getBytes()));
                sf.add(shipmentFile);
            }
        }
        return orderService.editMerchantOrderStatus(new EditOrderStatus(param.getMerchantId(),
                param.getOrderCode(), param.getStatus(), sf, param.getDeliveryType(),
                param.getExpressCompanyId(), param.getLogistiscCompanyName(), param.getNumber(),
                param.getDeliveryPerson(), param.getDeliveryPhoneNumber(), param.getModifier()));
    }
}
