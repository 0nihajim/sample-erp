package com.yufeng.controller.admin;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yufeng.entity.Log;
import com.yufeng.entity.ReturnList;
import com.yufeng.entity.ReturnListGoods;
import com.yufeng.service.LogService;
import com.yufeng.service.ReturnListGoodsService;
import com.yufeng.service.ReturnListService;
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
 * 返品伝票Controllerクラス
 *
 * @author Wensen Ma
 */
@RestController
@RequestMapping("/admin/returnList")
public class ReturnListAdminController {

    @Resource
    private ReturnListService returnListService;

    @Resource
    private ReturnListGoodsService returnListGoodsService;

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
     * 条件に基づいて返品伝票情報をページング検索
     *
     * @param returnList
     * @param page
     * @param rows
     * @return
     * @throws Exception
     */
    @RequestMapping("/list")
    @RequiresPermissions(value = {"返品伝票照会"})
    public Map<String, Object> list(ReturnList returnList) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<ReturnList> returnListList = returnListService.list(returnList, Direction.DESC, "returnDate");
        resultMap.put("rows", returnListList);
        return resultMap;
    }

    /**
     * 返品伝票IDに基づいて全ての返品商品を検索
     *
     * @param returnListId
     * @return
     * @throws Exception
     */
    @RequestMapping("/listGoods")
    @RequiresPermissions(value = {"返品伝票照会"})
    public Map<String, Object> listGoods(Integer returnListId) throws Exception {
        if (returnListId == null) {
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();
        List<ReturnListGoods> returnListGoodsList = returnListGoodsService.listByReturnListId(returnListId);
        resultMap.put("rows", returnListGoodsList);
        return resultMap;
    }

    /**
     * 顧客統計 返品伝票の全ての商品情報を取得
     *
     * @param purchaseList
     * @param purchaseListGoods
     * @return
     * @throws Exception
     */
    @RequestMapping("/listCount")
    @RequiresPermissions(value = {"顧客統計"})
    public Map<String, Object> listCount(ReturnList returnList, ReturnListGoods returnListGoods) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<ReturnList> returnListList = returnListService.list(returnList, Direction.DESC, "returnDate");
        for (ReturnList pl : returnListList) {
            returnListGoods.setReturnList(pl);
            List<ReturnListGoods> rlgList = returnListGoodsService.list(returnListGoods);
            for (ReturnListGoods rlg : rlgList) {
                rlg.setReturnList(null);
            }
            pl.setReturnListGoodsList(rlgList);
        }
        resultMap.put("rows", returnListList);
        return resultMap;
    }

    /**
     * 返品伝票番号を取得
     *
     * @param type
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/getReturnNumber")
    @RequiresPermissions(value = {"返品出庫"})
    public String genBillCode(String type) throws Exception {
        StringBuffer biilCodeStr = new StringBuffer();
        biilCodeStr.append("TH");
        biilCodeStr.append(DateUtil.getCurrentDateStr()); // 現在日付を連結
        String returnNumber = returnListService.getTodayMaxReturnNumber(); // 当日の最大返品伝票番号を取得
        if (returnNumber != null) {
            biilCodeStr.append(StringUtil.formatCode(returnNumber));
        } else {
            biilCodeStr.append("0001");
        }
        return biilCodeStr.toString();
    }

    /**
     * 返品伝票及び全ての返品商品を追加、商品の平均原価を修正
     *
     * @param returnList
     * @param goodsJson
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/save")
    @RequiresPermissions(value = {"返品出庫"})
    public Map<String, Object> save(ReturnList returnList, String goodsJson) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        returnList.setUser(userService.findByUserName((String) SecurityUtils.getSubject().getPrincipal())); // 操作ユーザーを設定
        Gson gson = new Gson();
        List<ReturnListGoods> plgList = gson.fromJson(goodsJson, new TypeToken<List<ReturnListGoods>>() {
        }.getType());
        returnListService.save(returnList, plgList);
        logService.save(new Log(Log.ADD_ACTION, "返品伝票を追加"));
        resultMap.put("success", true);
        return resultMap;
    }

    /**
     * 返品伝票の支払状態を修正
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
        ReturnList returnList = returnListService.findById(id);
        returnList.setState(1); // 支払状態に修正
        returnListService.update(returnList);
        resultMap.put("success", true);
        return resultMap;
    }

    /**
     * IDに基づいて返品伝票情報を削除 返品伝票内の商品を含む
     *
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping("/delete")
    @RequiresPermissions(value = {"返品伝票照会"})
    public Map<String, Object> delete(Integer id) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        returnListService.delete(id);
        logService.save(new Log(Log.DELETE_ACTION, "返品伝票情報を削除" + returnListService.findById(id)));  // ログを書き込む
        resultMap.put("success", true);
        return resultMap;
    }
}
