package com.yufeng.controller.admin;

import com.yufeng.entity.Goods;
import com.yufeng.entity.Log;
import com.yufeng.service.CustomerReturnListGoodsService;
import com.yufeng.service.GoodsService;
import com.yufeng.service.LogService;
import com.yufeng.service.SaleListGoodsService;
import com.yufeng.util.StringUtil;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * バックエンド商品管理Controller
 *
 * @author Wensen Ma
 */
@RestController
@RequestMapping("/admin/goods")
public class GoodsAdminContrller {

    @Resource
    private GoodsService goodsService;

    @Resource
    private SaleListGoodsService saleListGoodsService;

    @Resource
    private CustomerReturnListGoodsService customerReturnListGoodsService;

    @Resource
    private LogService logService;

    /**
     * 商品情報をページング検索
     */
    @RequestMapping("/list")
    @RequiresPermissions(value = {"商品管理", "仕入入庫"}, logical = Logical.OR)
    public Map<String, Object> list(Goods goods, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "rows", required = false) Integer rows) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<Goods> goodsList = goodsService.list(goods, page, rows, Direction.ASC, "id");
        Long total = goodsService.getCount(goods);
        resultMap.put("rows", goodsList);
        resultMap.put("total", total);
        logService.save(new Log(Log.SEARCH_ACTION, "商品情報を検索")); // ログを書き込む
        return resultMap;
    }

    /**
     * 商品在庫情報をページング検索
     */
    @RequestMapping("/listInventory")
    @RequiresPermissions(value = {"現在庫照会"})
    public Map<String, Object> listInventory(Goods goods, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "rows", required = false) Integer rows) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<Goods> goodsList = goodsService.list(goods, page, rows, Direction.ASC, "id");
        Long total = goodsService.getCount(goods);
        for (Goods g : goodsList) {
            g.setSaleTotal(saleListGoodsService.getTotalByGoodsId(g.getId()) - customerReturnListGoodsService.getTotalByGoodsId(g.getId())); // 販売総数を設定
        }
        resultMap.put("rows", goodsList);
        resultMap.put("total", total);
        logService.save(new Log(Log.SEARCH_ACTION, "商品在庫情報を検索"));
        return resultMap;
    }

    /**
     * 在庫警告商品を検索
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/listAlarm")
    @RequiresPermissions(value = {"在庫警告"})
    public Map<String, Object> listAlart() throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<Goods> alarmGoodsList = goodsService.listAlarm();
        resultMap.put("rows", alarmGoodsList);
        return resultMap;
    }

    /**
     * 在庫なし商品情報をページング検索
     *
     * @param codeOrName
     * @return
     * @throws Exception
     */
    @RequestMapping("/listNoInventoryQuantity")
    @RequiresPermissions(value = {"期初在庫"})
    public Map<String, Object> listNoInventoryQuantity(@RequestParam(value = "codeOrName", required = false) String codeOrName, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "rows", required = false) Integer rows) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<Goods> goodsList = goodsService.listNoInventoryQuantityByCodeOrName(codeOrName, page, rows, Direction.ASC, "id");
        Long total = goodsService.getCountNoInventoryQuantityByCodeOrName(codeOrName);
        resultMap.put("rows", goodsList);
        resultMap.put("total", total);
        logService.save(new Log(Log.SEARCH_ACTION, "商品情報を検索（在庫なし）")); // ログを書き込む
        return resultMap;
    }

    /**
     * 在庫あり商品情報をページング検索
     *
     * @param codeOrName
     * @return
     * @throws Exception
     */
    @RequestMapping("/listHasInventoryQuantity")
    @RequiresPermissions(value = {"期初在庫"})
    public Map<String, Object> listHasInventoryQuantity(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "rows", required = false) Integer rows) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<Goods> goodsList = goodsService.listHasInventoryQuantity(page, rows, Direction.ASC, "id");
        Long total = goodsService.getCountHasInventoryQuantity();
        resultMap.put("rows", goodsList);
        resultMap.put("total", total);
        logService.save(new Log(Log.SEARCH_ACTION, "商品情報を検索（在庫あり）")); // ログを書き込む
        return resultMap;
    }

    /**
     * 在庫を削除し、商品の在庫を0に設定
     *
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping("/deleteStock")
    @RequiresPermissions(value = {"期初在庫"})
    public Map<String, Object> deleteStock(Integer id) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        Goods goods = goodsService.findById(id);
        if (goods.getState() == 2) { // 入荷または販売伝票が発生している場合は削除不可
            resultMap.put("success", false);
            resultMap.put("errorInfo", "この商品は伝票が発生しているため削除できません！");
        } else {
            goods.setInventoryQuantity(0);
            goodsService.save(goods);
            resultMap.put("success", true);
        }
        return resultMap;
    }

    /**
     * 商品コードを生成
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/genGoodsCode")
    @RequiresPermissions(value = {"商品管理"})
    public String genGoodsCode() throws Exception {
        String maxGoodsCode = goodsService.getMaxGoodsCode();
        if (StringUtil.isNotEmpty(maxGoodsCode)) {
            Integer code = Integer.valueOf(maxGoodsCode) + 1;
            String codes = code.toString();
            int length = codes.length();
            for (int i = 4; i > length; i--) {
                codes = "0" + codes;
            }
            return codes;
        } else {
            return "0001";
        }
    }

    /**
     * 商品を追加
     *
     * @param goods
     * @return
     * @throws Exception
     */
    @RequestMapping("/save")
    @RequiresPermissions(value = {"商品管理", "仕入入庫"}, logical = Logical.OR)
    public Map<String, Object> save(Goods goods) throws Exception {
        if (goods.getId() != null) { // ログを書き込む
            logService.save(new Log(Log.UPDATE_ACTION, "商品情報を更新" + goods));
        } else {
            logService.save(new Log(Log.ADD_ACTION, "商品情報を追加" + goods));
            goods.setLastPurchasingPrice(goods.getPurchasingPrice()); // 前回入荷価格を現在価格に設定
        }
        Map<String, Object> resultMap = new HashMap<>();
        goodsService.save(goods);
        resultMap.put("success", true);
        return resultMap;
    }

    /**
     * 倉庫に商品を追加し、在庫情報を修正
     *
     * @param id
     * @param num
     * @param price
     * @return
     * @throws Exception
     */
    @RequestMapping("/saveStore")
    @RequiresPermissions(value = {"期初在庫"})
    public Map<String, Object> saveStore(Integer id, Integer num, Float price) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        Goods goods = goodsService.findById(id);
        goods.setInventoryQuantity(num);
        goods.setPurchasingPrice(price);
        goodsService.save(goods);
        logService.save(new Log(Log.UPDATE_ACTION, "商品" + goods + "を修正、価格=" + price + ",在庫=" + num)); // ログを書き込む
        resultMap.put("success", true);
        return resultMap;
    }

    /**
     * 商品情報を削除
     *
     * @param id
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/delete")
    @RequiresPermissions(value = {"商品管理"})
    public Map<String, Object> delete(Integer id) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        Goods goods = goodsService.findById(id);
        if (goods.getState() == 1) {
            resultMap.put("success", false);
            resultMap.put("errorInfo", "この商品は期初入庫されているため削除できません！");
        } else if (goods.getState() == 2) {
            resultMap.put("success", false);
            resultMap.put("errorInfo", "この商品は伝票が発生しているため削除できません！");
        } else {
            logService.save(new Log(Log.DELETE_ACTION, "商品情報を削除" + goodsService.findById(id)));  // ログを書き込む
            goodsService.delete(id);
            resultMap.put("success", true);
        }
        return resultMap;
    }
}
