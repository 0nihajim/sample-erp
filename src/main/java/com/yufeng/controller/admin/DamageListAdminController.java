package com.yufeng.controller.admin;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yufeng.entity.DamageList;
import com.yufeng.entity.DamageListGoods;
import com.yufeng.entity.Log;
import com.yufeng.service.DamageListGoodsService;
import com.yufeng.service.DamageListService;
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
 * 破損伝票Controllerクラス
 *
 * @author Wensen Ma
 */
@RestController
@RequestMapping("/admin/damageList")
public class DamageListAdminController {

    @Resource
    private DamageListService damageListService;

    @Resource
    private DamageListGoodsService damageListGoodsService;

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
     * 条件に基づいて破損伝票情報をページング検索
     *
     * @param damageList
     * @param page
     * @param rows
     * @return
     * @throws Exception
     */
    @RequestMapping("/list")
    @RequiresPermissions(value = {"損失過剰照会"})
    public Map<String, Object> list(DamageList damageList) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<DamageList> damageListList = damageListService.list(damageList, Direction.DESC, "damageDate");
        resultMap.put("rows", damageListList);
        return resultMap;
    }

    /**
     * 破損伝票IDに基づいて全ての破損商品を検索
     *
     * @param damageListId
     * @return
     * @throws Exception
     */
    @RequestMapping("/listGoods")
    @RequiresPermissions(value = {"損失過剰照会"})
    public Map<String, Object> listGoods(Integer damageListId) throws Exception {
        if (damageListId == null) {
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();
        List<DamageListGoods> damageListGoodsList = damageListGoodsService.listByDamageListId(damageListId);
        resultMap.put("rows", damageListGoodsList);
        return resultMap;
    }


    /**
     * 破損伝票番号を取得
     *
     * @param type
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/getDamageNumber")
    @RequiresPermissions(value = {"商品損失"})
    public String genBillCode(String type) throws Exception {
        StringBuffer biilCodeStr = new StringBuffer();
        biilCodeStr.append("BS");
        biilCodeStr.append(DateUtil.getCurrentDateStr()); // 現在日付を連結
        String damageNumber = damageListService.getTodayMaxDamageNumber(); // 当日の最大破損伝票番号を取得
        if (damageNumber != null) {
            biilCodeStr.append(StringUtil.formatCode(damageNumber));
        } else {
            biilCodeStr.append("0001");
        }
        return biilCodeStr.toString();
    }

    /**
     * 破損伝票と全ての破損商品を追加、および商品の平均原価を修正
     *
     * @param damageList
     * @param goodsJson
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/save")
    @RequiresPermissions(value = {"商品損失"})
    public Map<String, Object> save(DamageList damageList, String goodsJson) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        damageList.setUser(userService.findByUserName((String) SecurityUtils.getSubject().getPrincipal())); // 操作ユーザーを設定
        Gson gson = new Gson();
        List<DamageListGoods> plgList = gson.fromJson(goodsJson, new TypeToken<List<DamageListGoods>>() {
        }.getType());
        damageListService.save(damageList, plgList);
        logService.save(new Log(Log.ADD_ACTION, "破損伝票を追加"));
        resultMap.put("success", true);
        return resultMap;
    }

}
