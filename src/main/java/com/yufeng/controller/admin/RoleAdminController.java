package com.yufeng.controller.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.yufeng.entity.Log;
import com.yufeng.entity.Menu;
import com.yufeng.entity.Role;
import com.yufeng.entity.RoleMenu;
import com.yufeng.service.*;
import com.yufeng.util.StringUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * バックエンド管理ロールController
 *
 * @author Wensen Ma
 */
@RestController
@RequestMapping("/admin/role")
public class RoleAdminController {

    @Resource
    private RoleService roleService;

    @Resource
    private UserRoleService userRoleService;

    @Resource
    private MenuService menuService;

    @Resource
    private RoleMenuService roleMenuService;

    @Resource
    private LogService logService;

    /**
     * 全てのロールを検索
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/listAll")
    @RequiresPermissions(value = {"ロール管理"})
    public Map<String, Object> listAll() throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rows", roleService.listAll());
        logService.save(new Log(Log.SEARCH_ACTION, "全てのロール情報を検索")); // ログを書き込む
        return resultMap;
    }

    /**
     * ロール情報をページング検索
     *
     * @param user
     * @param page
     * @param rows
     * @return
     * @throws Exception
     */
    @RequestMapping("/list")
    @RequiresPermissions(value = {"ロール管理"})
    public Map<String, Object> list(Role role, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "rows", required = false) Integer rows) throws Exception {
        List<Role> roleList = roleService.list(role, page, rows, Direction.ASC, "id");
        Long total = roleService.getCount(role);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rows", roleList);
        resultMap.put("total", total);
        logService.save(new Log(Log.SEARCH_ACTION, "ロール情報を検索")); // ログを書き込む
        return resultMap;
    }

    /**
     * ロール情報を追加または修正
     *
     * @param role
     * @return
     * @throws Exception
     */
    @RequestMapping("/save")
    @RequiresPermissions(value = {"ロール管理"})
    public Map<String, Object> save(Role role) throws Exception {
        if (role.getId() != null) { // ログを書き込む
            logService.save(new Log(Log.UPDATE_ACTION, "ロール情報を更新" + role));
        } else {
            logService.save(new Log(Log.ADD_ACTION, "ロール情報を追加" + role));
        }
        Map<String, Object> resultMap = new HashMap<>();
        roleService.save(role);
        resultMap.put("success", true);
        return resultMap;
    }


    /**
     * ロール情報を削除
     *
     * @param id
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/delete")
    @RequiresPermissions(value = {"ロール管理"})
    public Map<String, Object> delete(Integer id) throws Exception {
        logService.save(new Log(Log.DELETE_ACTION, "ロール情報を削除" + roleService.findById(id)));  // ログを書き込む
        Map<String, Object> resultMap = new HashMap<>();
        userRoleService.deleteByRoleId(id); // ユーザーロール関連情報を削除
        roleService.delete(id);
        resultMap.put("success", true);
        return resultMap;
    }

    /**
     * 親ノードに基づいて全てのチェックボックス権限メニューツリーを取得
     *
     * @param parentId
     * @param roleId
     * @return
     * @throws Exception
     */
    @PostMapping("/loadCheckMenuInfo")
    @RequiresPermissions(value = {"ロール管理"})
    public String loadCheckMenuInfo(Integer parentId, Integer roleId) throws Exception {
        List<Menu> menuList = menuService.findByRoleId(roleId); // ロールに基づいて全ての権限メニュー情報を検索
        List<Integer> menuIdList = new LinkedList<Integer>();
        for (Menu menu : menuList) {
            menuIdList.add(menu.getId());
        }
        return getAllCheckedMenuByParentId(parentId, menuIdList).toString();
    }

    /**
     * 親ノードIDと権限メニューIDリストに基づいてチェックボックスメニューノードを取得
     *
     * @param parentId
     * @param menuIdList
     * @return
     */
    private JsonArray getAllCheckedMenuByParentId(Integer parentId, List<Integer> menuIdList) {
        JsonArray jsonArray = this.getCheckedMenuByParentId(parentId, menuIdList);
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = (JsonObject) jsonArray.get(i);
            if ("open".equals(jsonObject.get("state").getAsString())) {
                continue;
            } else {
                jsonObject.add("children", getAllCheckedMenuByParentId(jsonObject.get("id").getAsInt(), menuIdList));
            }
        }
        return jsonArray;
    }

    /**
     * 親ノードIDと権限メニューIDリストに基づいてチェックボックスメニューノードを取得
     *
     * @param parentId
     * @param menuIdList
     * @return
     */
    private JsonArray getCheckedMenuByParentId(Integer parentId, List<Integer> menuIdList) {
        List<Menu> menuList = menuService.findByParentId(parentId);
        JsonArray jsonArray = new JsonArray();
        for (Menu menu : menuList) {
            JsonObject jsonObject = new JsonObject();
            int menuId = menu.getId();
            jsonObject.addProperty("id", menuId); // ノードid
            jsonObject.addProperty("text", menu.getName()); // ノード名
            if (menu.getState() == 1) {
                jsonObject.addProperty("state", "closed"); // ルートノード
            } else {
                jsonObject.addProperty("state", "open"); // リーフノード
            }
            if (menuIdList.contains(menuId)) {
                jsonObject.addProperty("checked", true);
            }
            jsonObject.addProperty("iconCls", menu.getIcon());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /**
     * ロール権限設定を保存
     *
     * @param menuIds
     * @param roleId
     * @return
     * @throws Exception
     */
    @RequestMapping("/saveMenuSet")
    @RequiresPermissions(value = {"ロール管理"})
    public Map<String, Object> saveMenuSet(String menuIds, Integer roleId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        roleMenuService.deleteByRoleId(roleId); // ロールIDに基づいて全てのロール権限関連エンティティを削除
        if (StringUtil.isNotEmpty(menuIds)) {
            String[] idsStr = menuIds.split(",");
            for (int i = 0; i < idsStr.length; i++) { // その後、全てのロール権限関連エンティティを追加
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRole(roleService.findById(roleId));
                roleMenu.setMenu(menuService.findById(Integer.parseInt(idsStr[i])));
                roleMenuService.save(roleMenu);
            }
        }
        resultMap.put("success", true);
        logService.save(new Log(Log.ADD_ACTION, "ロール権限設定を保存"));  // ログを書き込む
        return resultMap;
    }
}
