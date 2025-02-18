package com.yufeng.controller.admin;

import com.yufeng.entity.GoodsUnit;
import com.yufeng.entity.Log;
import com.yufeng.service.GoodsUnitService;
import com.yufeng.service.LogService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * バックエンド商品単位Controller
 *
 * @author Wensen Ma
 */
@RestController
@RequestMapping("/admin/goodsUnit")
public class GoodsUnitAdminController {

    @Resource
    private GoodsUnitService goodsUnitService;

    @Resource
    private LogService logService;

    @RequestMapping("/comboList")
    @RequiresPermissions(value = {"商品管理"})
    public List<GoodsUnit> comboList() throws Exception {
        return goodsUnitService.listAll();
    }

    /**
     * 全ての商品単位を検索
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/listAll")
    @RequiresPermissions(value = {"商品管理", "入荷入庫"}, logical = Logical.OR)
    public Map<String, Object> listAll() throws Exception {
        List<GoodsUnit> goodsUnitList = goodsUnitService.listAll();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rows", goodsUnitList);
        logService.save(new Log(Log.SEARCH_ACTION, "商品単位情報を検索")); // ログを書き込む
        return resultMap;
    }

    /**
     * 商品単位を追加
     *
     * @param goodsUnit
     * @return
     * @throws Exception
     */
    @RequestMapping("/save")
    @RequiresPermissions(value = {"商品管理", "入荷入庫"}, logical = Logical.OR)
    public Map<String, Object> save(GoodsUnit goodsUnit) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        logService.save(new Log(Log.ADD_ACTION, "商品単位情報を追加" + goodsUnit));
        goodsUnitService.save(goodsUnit);
        resultMap.put("success", true);
        return resultMap;
    }

    /**
     * 商品単位情報を削除
     *
     * @param id
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/delete")
    @RequiresPermissions(value = {"商品管理", "入荷入庫"}, logical = Logical.OR)
    public Map<String, Object> delete(Integer id) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        logService.save(new Log(Log.DELETE_ACTION, "商品単位情報を削除" + goodsUnitService.findById(id)));  // ログを書き込む
        goodsUnitService.delete(id);
        resultMap.put("success", true);
        return resultMap;
    }

}
