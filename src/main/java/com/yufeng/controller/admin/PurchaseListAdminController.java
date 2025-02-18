package com.yufeng.controller.admin;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yufeng.entity.Log;
import com.yufeng.entity.PurchaseList;
import com.yufeng.entity.PurchaseListGoods;
import com.yufeng.service.LogService;
import com.yufeng.service.PurchaseListGoodsService;
import com.yufeng.service.PurchaseListService;
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
 * 仕入伝票Controllerクラス
 *
 * @author Wensen Ma
 */
@RestController
@RequestMapping("/admin/purchaseList")
public class PurchaseListAdminController {

    @Resource
    private PurchaseListService purchaseListService;

    @Resource
    private PurchaseListGoodsService purchaseListGoodsService;

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
     * 条件に基づいて仕入伝票情報をページング検索
     *
     * @param purchaseList
     * @param page
     * @param rows
     * @return
     * @throws Exception
     */
    @RequestMapping("/list")
    @RequiresPermissions(value = {"仕入伝票照会"})
    public Map<String, Object> list(PurchaseList purchaseList) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<PurchaseList> purchaseListList = purchaseListService.list(purchaseList, Direction.DESC, "purchaseDate");
        resultMap.put("rows", purchaseListList);
        return resultMap;
    }

    /**
     * 仕入伝票IDに基づいて全ての仕入商品を検索
     *
     * @param purchaseListId
     * @return
     * @throws Exception
     */
    @RequestMapping("/listGoods")
    @RequiresPermissions(value = {"仕入伝票照会"})
    public Map<String, Object> listGoods(Integer purchaseListId) throws Exception {
        if (purchaseListId == null) {
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();
        List<PurchaseListGoods> purchaseListGoodsList = purchaseListGoodsService.listByPurchaseListId(purchaseListId);
        resultMap.put("rows", purchaseListGoodsList);
        return resultMap;
    }

    /**
     * 顧客統計 仕入伝票の全ての商品情報を取得
     *
     * @param purchaseList
     * @param purchaseListGoods
     * @return
     * @throws Exception
     */
    @RequestMapping("/listCount")
    @RequiresPermissions(value = {"顧客統計"})
    public Map<String, Object> listCount(PurchaseList purchaseList, PurchaseListGoods purchaseListGoods) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<PurchaseList> purchaseListList = purchaseListService.list(purchaseList, Direction.DESC, "purchaseDate");
        for (PurchaseList pl : purchaseListList) {
            purchaseListGoods.setPurchaseList(pl);
            List<PurchaseListGoods> plgList = purchaseListGoodsService.list(purchaseListGoods);
            for (PurchaseListGoods plg : plgList) {
                plg.setPurchaseList(null);
            }
            pl.setPurchaseListGoodsList(plgList);
        }
        resultMap.put("rows", purchaseListList);
        return resultMap;
    }

    /**
     * 仕入伝票番号を取得
     *
     * @param type
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/getPurchaseNumber")
    @RequiresPermissions(value = {"仕入入庫"})
    public String genBillCode(String type) throws Exception {
        StringBuffer biilCodeStr = new StringBuffer();
        biilCodeStr.append("JH");
        biilCodeStr.append(DateUtil.getCurrentDateStr()); // 現在日付を連結
        String purchaseNumber = purchaseListService.getTodayMaxPurchaseNumber(); // 当日の最大仕入伝票番号を取得
        if (purchaseNumber != null) {
            biilCodeStr.append(StringUtil.formatCode(purchaseNumber));
        } else {
            biilCodeStr.append("0001");
        }
        return biilCodeStr.toString();
    }

    /**
     * 仕入伝票及び全ての仕入商品を追加、商品の平均原価を修正
     *
     * @param purchaseList
     * @param goodsJson
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/save")
    @RequiresPermissions(value = {"仕入入庫"})
    public Map<String, Object> save(PurchaseList purchaseList, String goodsJson) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        purchaseList.setUser(userService.findByUserName((String) SecurityUtils.getSubject().getPrincipal())); // 操作ユーザーを設定
        Gson gson = new Gson();
        List<PurchaseListGoods> plgList = gson.fromJson(goodsJson, new TypeToken<List<PurchaseListGoods>>() {
        }.getType());
        purchaseListService.save(purchaseList, plgList);
        logService.save(new Log(Log.ADD_ACTION, "仕入伝票を追加"));
        resultMap.put("success", true);
        return resultMap;
    }

    /**
     * 仕入伝票の支払状態を修正
     *
     * @param id
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/update")
    @RequiresPermissions(value = {"仕入先統計"})
    public Map<String, Object> update(Integer id) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        PurchaseList purchaseList = purchaseListService.findById(id);
        purchaseList.setState(1); // 支払状態に修正
        purchaseListService.update(purchaseList);
        resultMap.put("success", true);
        return resultMap;
    }

    /**
     * IDに基づいて仕入伝票情報を削除 仕入伝票内の商品を含む
     *
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping("/delete")
    @RequiresPermissions(value = {"仕入伝票照会"})
    public Map<String, Object> delete(Integer id) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        purchaseListService.delete(id);
        logService.save(new Log(Log.DELETE_ACTION, "仕入伝票情報を削除" + purchaseListService.findById(id)));  // ログを書き込む
        resultMap.put("success", true);
        return resultMap;
    }


}
