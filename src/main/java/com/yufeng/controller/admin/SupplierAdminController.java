package com.yufeng.controller.admin;

import com.yufeng.entity.Log;
import com.yufeng.entity.Supplier;
import com.yufeng.service.LogService;
import com.yufeng.service.SupplierService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * バックエンド管理仕入先Controller
 *
 * @author Wensen Ma
 */
@RestController
@RequestMapping("/admin/supplier")
public class SupplierAdminController {

    @Resource
    private SupplierService supplierService;

    @Resource
    private LogService logService;

    /**
     * 仕入先情報をページング検索
     *
     * @param supplier
     * @param page
     * @param rows
     * @return
     * @throws Exception
     */
    @RequestMapping("/list")
    @RequiresPermissions(value = {"仕入先管理"})
    public Map<String, Object> list(Supplier supplier, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "rows", required = false) Integer rows) throws Exception {
        List<Supplier> supplierList = supplierService.list(supplier, page, rows, Direction.ASC, "id");
        Long total = supplierService.getCount(supplier);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rows", supplierList);
        resultMap.put("total", total);
        logService.save(new Log(Log.SEARCH_ACTION, "仕入先情報を検索")); // ログを書き込む
        return resultMap;
    }

    /**
     * ドロップダウンリストのあいまい検索
     *
     * @param q
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/comboList")
    @RequiresPermissions(value = {"仕入入庫", "返品出庫", "仕入伝票照会", "返品伝票照会"}, logical = Logical.OR)
    public List<Supplier> comboList(String q) throws Exception {
        if (q == null) {
            q = "";
        }
        return supplierService.findByName("%" + q + "%");
    }

    /**
     * 仕入先情報を追加または修正
     *
     * @param supplier
     * @return
     * @throws Exception
     */
    @RequestMapping("/save")
    @RequiresPermissions(value = {"仕入先管理"})
    public Map<String, Object> save(Supplier supplier) throws Exception {
        if (supplier.getId() != null) { // ログを書き込む
            logService.save(new Log(Log.UPDATE_ACTION, "仕入先情報を更新" + supplier));
        } else {
            logService.save(new Log(Log.ADD_ACTION, "仕入先情報を追加" + supplier));
        }
        Map<String, Object> resultMap = new HashMap<>();
        supplierService.save(supplier);
        resultMap.put("success", true);
        return resultMap;
    }

    /**
     * 仕入先情報を削除
     *
     * @param id
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/delete")
    @RequiresPermissions(value = {"仕入先管理"})
    public Map<String, Object> delete(String ids) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        String[] idsStr = ids.split(",");
        for (int i = 0; i < idsStr.length; i++) {
            int id = Integer.parseInt(idsStr[i]);
            logService.save(new Log(Log.DELETE_ACTION, "仕入先情報を削除" + supplierService.findById(id)));  // ログを書き込む
            supplierService.delete(id);
        }
        resultMap.put("success", true);
        return resultMap;
    }

}
