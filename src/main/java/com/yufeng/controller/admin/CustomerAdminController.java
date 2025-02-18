package com.yufeng.controller.admin;

import com.yufeng.entity.Customer;
import com.yufeng.entity.Log;
import com.yufeng.service.CustomerService;
import com.yufeng.service.LogService;
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
 * バックエンド顧客管理Controller
 *
 * @author Wensen Ma
 */
@RestController
@RequestMapping("/admin/customer")
public class CustomerAdminController {

    @Resource
    private CustomerService customerService;

    @Resource
    private LogService logService;

    /**
     * 顧客情報をページング検索
     *
     * @param customer
     * @param page
     * @param rows
     * @return
     * @throws Exception
     */
    @RequestMapping("/list")
    @RequiresPermissions(value = {"顧客管理"})
    public Map<String, Object> list(Customer customer, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "rows", required = false) Integer rows) throws Exception {
        List<Customer> customerList = customerService.list(customer, page, rows, Direction.ASC, "id");
        Long total = customerService.getCount(customer);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rows", customerList);
        resultMap.put("total", total);
        logService.save(new Log(Log.SEARCH_ACTION, "顧客情報を検索")); // ログを書き込む
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
    @RequiresPermissions(value = {"販売出庫", "顧客返品", "販売伝票照会", "顧客返品照会"}, logical = Logical.OR)
    public List<Customer> comboList(String q) throws Exception {
        if (q == null) {
            q = "";
        }
        return customerService.findByName("%" + q + "%");
    }


    /**
     * 顧客情報を追加または更新
     *
     * @param customer
     * @return
     * @throws Exception
     */
    @RequestMapping("/save")
    @RequiresPermissions(value = {"顧客管理"})
    public Map<String, Object> save(Customer customer) throws Exception {
        if (customer.getId() != null) {
            logService.save(new Log(Log.UPDATE_ACTION, "顧客情報を更新" + customer));
        } else {
            logService.save(new Log(Log.ADD_ACTION, "顧客情報を追加" + customer));
        }
        Map<String, Object> resultMap = new HashMap<>();
        customerService.save(customer);
        resultMap.put("success", true);
        return resultMap;
    }


    /**
     * 顧客情報を削除
     *
     * @param id
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/delete")
    @RequiresPermissions(value = {"顧客管理"})
    public Map<String, Object> delete(String ids) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        String[] idsStr = ids.split(",");
        for (int i = 0; i < idsStr.length; i++) {
            int id = Integer.parseInt(idsStr[i]);
            logService.save(new Log(Log.DELETE_ACTION, "顧客情報を削除" + customerService.findById(id)));
            customerService.delete(id);
        }
        resultMap.put("success", true);
        return resultMap;
    }

}