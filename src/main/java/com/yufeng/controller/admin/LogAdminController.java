package com.yufeng.controller.admin;

import com.yufeng.entity.Log;
import com.yufeng.service.LogService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * バックエンドシステムログController
 *
 * @author Wensen Ma
 */
@RestController
@RequestMapping("/admin/log")
public class LogAdminController {

    @Resource
    private LogService logService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(true);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));   //true:空値入力可能、false:空値不可
    }

    /**
     * 条件に基づいてログ情報をページング検索
     *
     * @param goods
     * @param page
     * @param rows
     * @return
     * @throws Exception
     */
    @RequestMapping("/list")
    @RequiresPermissions(value = {"システムログ"})
    public Map<String, Object> list(Log log, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "rows", required = false) Integer rows) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<Log> logList = logService.list(log, page, rows, Direction.DESC, "time");
        Long total = logService.getCount(log);
        resultMap.put("rows", logList);
        resultMap.put("total", total);
        return resultMap;
    }
}
