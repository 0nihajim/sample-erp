package com.yufeng.controller.admin;

import com.yufeng.entity.Log;
import com.yufeng.entity.Role;
import com.yufeng.entity.User;
import com.yufeng.entity.UserRole;
import com.yufeng.service.LogService;
import com.yufeng.service.RoleService;
import com.yufeng.service.UserRoleService;
import com.yufeng.service.UserService;
import com.yufeng.util.StringUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * バックエンド管理ユーザーController
 *
 * @author Wensen Ma
 */
@Controller
@RequestMapping("/admin/user")
public class UserAdminController {

    @Resource
    private UserService userService;

    @Resource
    private RoleService roleService;

    @Resource
    private UserRoleService userRoleService;

    @Resource
    private LogService logService;

    /**
     * パスワードを変更
     *
     * @param id
     * @param newPassword
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/modifyPassword")
    @RequiresPermissions(value = {"パスワード変更"})
    public Map<String, Object> modifyPassword(Integer id, String newPassword, HttpSession session) throws Exception {
        User currentUser = (User) session.getAttribute("currentUser");
        User user = userService.findById(currentUser.getId());
        user.setPassword(newPassword);
        userService.save(user);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("success", true);
        logService.save(new Log(Log.UPDATE_ACTION, "パスワードを変更")); // ログを書き込む
        return map;
    }

    /**
     * 安全にログアウト
     *
     * @return
     * @throws Exception
     */
    @GetMapping("/logout")
    @RequiresPermissions(value = {"安全にログアウト"})
    public String logout() throws Exception {
        logService.save(new Log(Log.LOGOUT_ACTION, "ユーザーがログアウト"));
        SecurityUtils.getSubject().logout();
        return "redirect:/login.html";
    }

    /**
     * ユーザー情報をページング検索
     *
     * @param user
     * @param page
     * @param rows
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/list")
    @RequiresPermissions(value = {"ユーザー管理"})
    public Map<String, Object> list(User user, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "rows", required = false) Integer rows) throws Exception {
        List<User> userList = userService.list(user, page, rows, Direction.ASC, "id");
        for (User u : userList) {
            List<Role> roleList = roleService.findByUserId(u.getId());
            StringBuffer sb = new StringBuffer();
            for (Role r : roleList) {
                sb.append("," + r.getName());
            }
            u.setRoles(sb.toString().replaceFirst(",", ""));
        }
        Long total = userService.getCount(user);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rows", userList);
        resultMap.put("total", total);
        logService.save(new Log(Log.SEARCH_ACTION, "ユーザー情報を検索")); // ログを書き込む
        return resultMap;
    }

    /**
     * ユーザーロール設定を保存
     *
     * @param roleIds
     * @param userId
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/saveRoleSet")
    @RequiresPermissions(value = {"ユーザー管理"})
    public Map<String, Object> saveRoleSet(String roleIds, Integer userId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        userRoleService.deleteByUserId(userId);  // ユーザーIDに基づいて全てのユーザーロール関連エンティティを削除
        if (StringUtil.isNotEmpty(roleIds)) {
            String[] idsStr = roleIds.split(",");
            for (int i = 0; i < idsStr.length; i++) { // その後、全てのユーザーロール関連エンティティを追加
                UserRole userRole = new UserRole();
                userRole.setUser(userService.findById(userId));
                userRole.setRole(roleService.findById(Integer.parseInt(idsStr[i])));
                userRoleService.save(userRole);
            }
        }
        resultMap.put("success", true);
        logService.save(new Log(Log.UPDATE_ACTION, "ユーザーロール設定を保存"));
        return resultMap;
    }

    /**
     * ユーザー情報を追加または更新
     *
     * @param user
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/save")
    @RequiresPermissions(value = {"ユーザー管理"})
    public Map<String, Object> save(User user) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        if (user.getId() == null) {
            if (userService.findByUserName(user.getUserName()) != null) {
                resultMap.put("success", false);
                resultMap.put("errorInfo", "ユーザー名が既に存在します!");
                return resultMap;
            }
        }
        if (user.getId() != null) { // ログを書き込む
            logService.save(new Log(Log.UPDATE_ACTION, "ユーザー情報を更新" + user));
        } else {
            logService.save(new Log(Log.ADD_ACTION, "ユーザー情報を追加" + user));
        }
        userService.save(user);
        resultMap.put("success", true);
        return resultMap;
    }

    /**
     * ユーザー情報を削除
     *
     * @param id
     * @param response
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/delete")
    @RequiresPermissions(value = {"ユーザー管理"})
    public Map<String, Object> delete(Integer id) throws Exception {
        logService.save(new Log(Log.DELETE_ACTION, "ユーザー情報を削除" + userService.findById(id)));  // ログを書き込む
        Map<String, Object> resultMap = new HashMap<>();
        userRoleService.deleteByUserId(id); // ユーザーロール関連情報を削除
        userService.delete(id);
        resultMap.put("success", true);
        return resultMap;
    }
}
