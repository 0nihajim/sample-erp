package com.yufeng.controller.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.yufeng.entity.GoodsType;
import com.yufeng.entity.Log;
import com.yufeng.service.GoodsTypeService;
import com.yufeng.service.LogService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * バックエンド商品カテゴリController
 *
 * @author Wensen Ma
 */
@RestController
@RequestMapping("/admin/goodsType")
public class GoodsTypeAdminController {

    @Resource
    private GoodsTypeService goodsTypeService;

    @Resource
    private LogService logService;

    /**
     * 商品カテゴリツリーメニューを読み込む
     *
     * @return
     * @throws Exception
     */
    @PostMapping("/loadTreeInfo")
    @RequiresPermissions(value = {"商品管理", "仕入入庫", "現在庫照会"}, logical = Logical.OR)
    public String loadTreeInfo() throws Exception {
        logService.save(new Log(Log.SEARCH_ACTION, "商品カテゴリ情報を検索")); // ログを書き込む
        return getAllByParentId(-1).toString();
    }

    /**
     * 商品カテゴリを追加
     *
     * @param name
     * @param parentId
     * @return
     * @throws Exception
     */
    @RequestMapping("/save")
    @RequiresPermissions(value = {"商品管理", "仕入入庫"}, logical = Logical.OR)
    public Map<String, Object> save(String name, Integer parentId) throws Exception {

        Map<String, Object> resultMap = new HashMap<>();
        GoodsType goodsType = new GoodsType();
        goodsType.setName(name);
        goodsType.setpId(parentId);
        goodsType.setIcon("icon-folder");
        goodsType.setState(0);
        logService.save(new Log(Log.ADD_ACTION, "商品カテゴリ情報を追加" + goodsType));
        goodsTypeService.save(goodsType); // 商品カテゴリを保存

        GoodsType parentGoodsType = goodsTypeService.findById(parentId); // 親ノードを検索
        parentGoodsType.setState(1); // stateを1に変更 ルートノード
        goodsTypeService.save(parentGoodsType); // 親ノードの商品カテゴリを保存

        resultMap.put("success", true);
        return resultMap;
    }

    /**
     * 商品カテゴリを削除
     *
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping("/delete")
    @RequiresPermissions(value = {"商品管理", "入荷入庫"}, logical = Logical.OR)
    public Map<String, Object> delete(Integer id) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        GoodsType goodsType = goodsTypeService.findById(id);
        if (goodsTypeService.findByParentId(goodsType.getpId()).size() == 1) { // 親ノードの下に現在のサブノードしかない場合、親ノードのstate状態を変更
            GoodsType parentGoodsType = goodsTypeService.findById(goodsType.getpId());
            parentGoodsType.setState(0); // stateを0に変更 リーフノード
            goodsTypeService.save(parentGoodsType); // 親ノードの商品カテゴリを保存
        }
        logService.save(new Log(Log.DELETE_ACTION, "商品カテゴリ情報を削除" + goodsType)); // ログを書き込む
        goodsTypeService.delete(id); // 削除
        resultMap.put("success", true);
        return resultMap;
    }

    /**
     * 親ノードに基づいて全ての商品カテゴリ情報を再帰的に取得
     *
     * @param parentId
     * @return
     */
    public JsonArray getAllByParentId(Integer parentId) {
        JsonArray jsonArray = this.getByParentId(parentId);
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = (JsonObject) jsonArray.get(i);
            if ("open".equals(jsonObject.get("state").getAsString())) {
                continue;
            } else {
                jsonObject.add("children", getAllByParentId(jsonObject.get("id").getAsInt()));
            }
        }
        return jsonArray;
    }

    /**
     * 親ノードに基づいてサブノードを検索
     *
     * @param parentId
     * @return
     */
    private JsonArray getByParentId(Integer parentId) {
        JsonArray jsonArray = new JsonArray();
        List<GoodsType> goodsTypeList = goodsTypeService.findByParentId(parentId);
        for (GoodsType goodsType : goodsTypeList) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", goodsType.getId()); // ノードID
            jsonObject.addProperty("text", goodsType.getName()); // ノード名
            if (goodsType.getState() == 1) {
                jsonObject.addProperty("state", "closed"); // ルートノード
            } else {
                jsonObject.addProperty("state", "open"); // リーフノード
            }
            jsonObject.addProperty("iconCls", goodsType.getIcon());
            JsonObject attributeObject = new JsonObject(); // 拡張属性
            attributeObject.addProperty("state", goodsType.getState()); // ノード状態
            jsonObject.add("attributes", attributeObject);
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }


}
