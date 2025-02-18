package com.yufeng.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.yufeng.entity.Log;
import com.yufeng.entity.Menu;
import com.yufeng.entity.Role;
import com.yufeng.entity.User;
import com.yufeng.service.LogService;
import com.yufeng.service.MenuService;
import com.yufeng.service.RoleService;
import com.yufeng.service.UserService;
import com.yufeng.util.StringUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 現在ログインしているユーザーのコントローラー
 *
 * @author Wensen Ma
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Resource
    private RoleService roleService;

    @Resource
    private UserService userService;

    @Resource
    private MenuService menuService;

    @Resource
    private LogService logService;

    /**
     * ユーザーログインリクエスト
     *
     * @param user
     * @return
     */
    @ResponseBody
    @PostMapping("/login")
    public Map<String, Object> login(String imageCode, User user, BindingResult bindingResult, HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (StringUtil.isEmpty(imageCode)) {
            map.put("success", false);
            map.put("errorInfo", "認証コードを入力してください！");
            return map;
        }
        if (!session.getAttribute("checkcode").equals(imageCode)) {
            map.put("success", false);
            map.put("errorInfo", "認証コードが間違っています！");
            return map;
        }
        if (bindingResult.hasErrors()) {
            map.put("success", false);
            map.put("errorInfo", bindingResult.getFieldError().getDefaultMessage());
            return map;
        }
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(user.getUserName(), user.getPassword());
        try {
            subject.login(token); // ログイン認証
            String userName = (String) SecurityUtils.getSubject().getPrincipal();
            User currentUser = userService.findByUserName(userName);
            session.setAttribute("currentUser", currentUser);
            List<Role> roleList = roleService.findByUserId(currentUser.getId());
            map.put("roleList", roleList);
            map.put("roleSize", roleList.size());
            map.put("success", true);
            logService.save(new Log(Log.LOGIN_ACTION, "ユーザーログイン")); // ログを記録
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            map.put("success", false);
            map.put("errorInfo", "ユーザー名またはパスワードが間違っています！");
            return map;
        }
    }

    /**
     * ロール情報を保存
     *
     * @param roleId
     * @param session
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/saveRole")
    public Map<String, Object> saveRole(Integer roleId, HttpSession session) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Role currentRole = roleService.findById(roleId);
        session.setAttribute("currentRole", currentRole); // 現在のロール情報を保存
        map.put("success", true);
        return map;
    }

    /**
     * 現在のユーザー情報を読み込む
     *
     * @param session
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/loadUserInfo")
    public String loadUserInfo(HttpSession session) throws Exception {
        User currentUser = (User) session.getAttribute("currentUser");
        Role currentRole = (Role) session.getAttribute("currentRole");
        return "ようこそ：" + currentUser.getTrueName() + "&nbsp;[&nbsp;" + currentRole.getName() + "&nbsp;]";
    }

    /**
     * 権限メニューを読み込む
     *
     * @param session
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/loadMenuInfo")
    public String loadMenuInfo(HttpSession session, Integer parentId) throws Exception {
        Role currentRole = (Role) session.getAttribute("currentRole");
        return getAllMenuByParentId(parentId, currentRole.getId()).toString();
    }

    /**
     * すべてのメニュー情報を取得
     *
     * @param parentId
     * @param roleId
     * @return
     */
    private JsonArray getAllMenuByParentId(Integer parentId, Integer roleId) {
        JsonArray jsonArray = this.getMenuByParentId(parentId, roleId);
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = (JsonObject) jsonArray.get(i);
            if ("open".equals(jsonObject.get("state").getAsString())) {
                continue;
            } else {
                jsonObject.add("children", getAllMenuByParentId(jsonObject.get("id").getAsInt(), roleId));
            }
        }
        return jsonArray;
    }

    /**
     * 親ノードとユーザーロールIDによってメニューを検索
     *
     * @param parentId
     * @param roleId
     * @return
     */
    private JsonArray getMenuByParentId(Integer parentId, Integer roleId) {
        List<Menu> menuList = menuService.findByParentIdAndRoleId(parentId, roleId);
        JsonArray jsonArray = new JsonArray();
        for (Menu menu : menuList) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", menu.getId()); // ノードID
            jsonObject.addProperty("text", menu.getName()); // ノード名
            if (menu.getState() == 1) {
                jsonObject.addProperty("state", "closed"); // ルートノード
            } else {
                jsonObject.addProperty("state", "open"); // リーフノード
            }
            jsonObject.addProperty("iconCls", menu.getIcon());
            JsonObject attributeObject = new JsonObject(); // 拡張属性
            attributeObject.addProperty("url", menu.getUrl()); // メニューリクエストアドレス
            jsonObject.add("attributes", attributeObject);
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }
}
