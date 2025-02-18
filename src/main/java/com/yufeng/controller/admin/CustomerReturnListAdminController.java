package com.yufeng.controller.admin;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yufeng.entity.CustomerReturnList;
import com.yufeng.entity.CustomerReturnListGoods;
import com.yufeng.entity.Log;
import com.yufeng.service.CustomerReturnListGoodsService;
import com.yufeng.service.CustomerReturnListService;
import com.yufeng.service.LogService;
import com.yufeng.service.UserService;
import com.yufeng.util.DateUtil;
import com.yufeng.util.StringUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 顧客返品伝票Controllerクラス
 *
 * @author Wensen Ma
 */
@RestController
@RequestMapping("/admin/customerReturnList")
public class CustomerReturnListAdminController {

    @Resource
    private CustomerReturnListService customerReturnListService;

    @Resource
    private CustomerReturnListGoodsService customerReturnListGoodsService;

    @Resource
    private LogService logService;

    @Resource
    private UserService userService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(true);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));   //true:空値入力可能、false:空値不可
    }

    /**
     * 条件に基づいて顧客返品伝票情報をページング検索
     *
     * @param customerReturnList
     * @param page
     * @param rows
     * @return
     * @throws Exception
     */
    @RequestMapping("/list")
    @RequiresPermissions(value = {"顧客返品照会"})
    public Map<String, Object> list(CustomerReturnList customerReturnList) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<CustomerReturnList> customerReturnListList = customerReturnListService.list(customerReturnList, Direction.DESC, "customerReturnDate");
        resultMap.put("rows", customerReturnListList);
        return resultMap;
    }

    /**
     * 顧客返品伝票IDに基づいて全ての返品商品を検索
     *
     * @param customerReturnListId
     * @return
     * @throws Exception
     */
    @RequestMapping("/listGoods")
    @RequiresPermissions(value = {"顧客返品照会"})
    public Map<String, Object> listGoods(Integer customerReturnListId) throws Exception {
        if (customerReturnListId == null) {
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();
        List<CustomerReturnListGoods> customerReturnListGoodsList = customerReturnListGoodsService.listByCustomerReturnListId(customerReturnListId);
        resultMap.put("rows", customerReturnListGoodsList);
        return resultMap;
    }

    /**
     * 顧客統計 顧客返品伝票の全商品情報を取得
     *
     * @param purchaseList
     * @param purchaseListGoods
     * @return
     * @throws Exception
     */
    @RequestMapping("/listCount")
    @RequiresPermissions(value = {"顧客統計"})
    public Map<String, Object> listCount(CustomerReturnList customerReturnList, CustomerReturnListGoods customerReturnListGoods) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<CustomerReturnList> customerReturnListList = customerReturnListService.list(customerReturnList, Direction.DESC, "customerReturnDate");
        for (CustomerReturnList crl : customerReturnListList) {
            customerReturnListGoods.setCustomerReturnList(crl);

            List<CustomerReturnListGoods> crlList = customerReturnListGoodsService.list(customerReturnListGoods);
            for (CustomerReturnListGoods crlg : crlList) {
                crlg.setCustomerReturnList(null);
            }
            crl.setCustomerReturnListGoodsList(crlList);
        }
        resultMap.put("rows", customerReturnListList);
        return resultMap;
    }


    /**
     * 顧客返品伝票番号を取得
     *
     * @param type
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/getCustomerReturnNumber")
    @RequiresPermissions(value = {"顧客返品"})
    public String genBillCode(String type) throws Exception {
        StringBuffer biilCodeStr = new StringBuffer();
        biilCodeStr.append("XT");
        biilCodeStr.append(DateUtil.getCurrentDateStr()); // 現在日付を連結
        String customerReturnNumber = customerReturnListService.getTodayMaxCustomerReturnNumber(); // 当日の最大顧客返品伝票番号を取得
        if (customerReturnNumber != null) {
            biilCodeStr.append(StringUtil.formatCode(customerReturnNumber));
        } else {
            biilCodeStr.append("0001");
        }
        return biilCodeStr.toString();
    }

    /**
     * 顧客返品伝票と全ての返品商品を追加
     *
     * @param customerReturnList
     * @param goodsJson
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/save")
    @RequiresPermissions(value = {"顧客返品"})
    public Map<String, Object> save(CustomerReturnList customerReturnList, String goodsJson) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        customerReturnList.setUser(userService.findByUserName((String) SecurityUtils.getSubject().getPrincipal())); // 操作ユーザーを設定
        Gson gson = new Gson();
        List<CustomerReturnListGoods> plgList = gson.fromJson(goodsJson, new TypeToken<List<CustomerReturnListGoods>>() {
        }.getType());
        customerReturnListService.save(customerReturnList, plgList);
        logService.save(new Log(Log.ADD_ACTION, "顧客返品伝票を追加"));
        resultMap.put("success", true);
        return resultMap;
    }

    /**
     * 返品伝票の支払状態を更新
     *
     * @param id
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/update")
    @RequiresPermissions(value = {"顧客統計"})
    public Map<String, Object> update(Integer id) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        CustomerReturnList customerReturnList = customerReturnListService.findById(id);
        customerReturnList.setState(1); // 支払済状態に更新
        customerReturnListService.update(customerReturnList);
        resultMap.put("success", true);
        return resultMap;
    }

    /**
     * IDに基づいて顧客返品伝票情報を削除（返品商品を含む）
     *
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping("/delete")
    @RequiresPermissions(value = {"顧客返品照会"})
    public Map<String, Object> delete(Integer id) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        customerReturnListService.delete(id);
        logService.save(new Log(Log.DELETE_ACTION, "顧客返品伝票情報を削除" + customerReturnListService.findById(id)));  // ログを書き込む
        resultMap.put("success", true);
        return resultMap;
    }
}