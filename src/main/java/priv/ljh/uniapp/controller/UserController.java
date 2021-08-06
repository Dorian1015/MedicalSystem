package priv.ljh.uniapp.controller;


import cn.hutool.core.util.RandomUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import priv.ljh.uniapp.entity.User;
import priv.ljh.uniapp.mapper.EventsMapper;
import priv.ljh.uniapp.mapper.PatientDataMapper;
import priv.ljh.uniapp.mapper.UserMapper;
import priv.ljh.uniapp.service.UserService;
import priv.ljh.utils.Constants;
import priv.ljh.utils.MyPage;
import priv.ljh.utils.PCJwtUtils;
import priv.ljh.utils.ResultResponse;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  PC端用户信息 前端控制器
 * </p>
 *
 * @author lijinghai
 * @since 2021-05-28
 */
@Api(tags = {"用户控制类 登录"})
@Slf4j
@RestController
@RequestMapping("/uniappuser")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PatientDataMapper patientDataMapper;

    @Autowired
    private EventsMapper eventsMapper;

    @ApiOperation("用户登录,用户登录时的第一道关卡")
    @PostMapping("/login")
    public ResultResponse login(@RequestBody User user,HttpServletRequest request){
        log.info("用户名:"+user.getAccount());
        log.info("密码:"+user.getPassword());


        ResultResponse res = null;
        Map<String,Object> map = new HashMap<>();
        try {
            User userDB = userService.login(user);
            Map<String,String> playload = new HashMap<>();
            playload.put("account",userDB.getAccount());

            //生成JWT令牌机制
            String token = PCJwtUtils.getToken(playload);

            map.put("token",token);
            map.put("id",userDB.getId());
            map.put("account",userDB.getAccount());
            map.put("state",1);

            //设置ServletContext

            ServletContext context= request.getServletContext();

            context.setAttribute("id",userDB.getId());

            // 获取
            context.getAttribute("id");

            log.info("user id======>"+ context.getAttribute("id"));

            res = new ResultResponse(Constants.STATUS_OK, Constants.MESSAGE_OK, map);

        } catch (Exception e) {
            map.put("message",e.getMessage());
            res = new ResultResponse(Constants.STATUS_FALL, Constants.MESSAGE_FALL, map);
        }

        return res;
    }

    @ApiOperation("验证用户是否登录，用户登录的第二道关卡")
    @PostMapping("/validate")
    public ResultResponse validate(HttpServletRequest request){
        Map<String,Object> map = new HashMap<>();
        ResultResponse res = null;
        //验证Token的合法性
        String token = request.getHeader("Authorization");
        DecodedJWT verify = PCJwtUtils.verify(token);
        //验证成功则获取用户名
        map.put("mobile",verify.getClaim("mobile").asString());
        map.put("state",1);
        log.info("登录成功");
        res = new ResultResponse(Constants.STATUS_OK, Constants.MESSAGE_OK, map);

        return res;
    }


    @ApiOperation("增加一条用户信息")
    @PostMapping("/add")
    public ResultResponse create(@RequestBody User user,@RequestParam(value = "account", required = false) String account,@RequestParam(value = "password", required = false) String password) throws SQLException {
        int ret = 0;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ResultResponse res = null;
        int id = RandomUtil.randomInt(10000);
//        int insert = userMapper.insert(user);
        int count = userMapper.insertUser(user.getAccount(), user.getPassword());
//        userMapper.selectId(user.getAccount());
//        log.info("id===========>"+user.getId());
//        // 添加到病患资料表
//        patientDataMapper.InsertPatient(user.getId());
        log.info("count===>"+count);
        if (count == 1 ){
//            userMapper.insert(user);

            int i = userMapper.selectId(user.getAccount());
            log.info("id===========>"+i);
            // 添加到病患资料表
            patientDataMapper.InsertPatient(i);
            res = new ResultResponse(Constants.STATUS_OK, Constants.MESSAGE_OK, user);
        } else {
            String message = "用户名已经存在，请重新输入";
            log.info("message===========>"+message);
            res = new ResultResponse(Constants.STATUS_FALL, Constants.MESSAGE_FALL+message,null);
        }
        return res;
    }


    @ApiOperation("根据id删除一条用户数据")
    @PostMapping("/delete")
    public ResultResponse deleteUser (@RequestParam("id") Integer id){
        ResultResponse res = null;
        int result = userMapper.deleteById(id);
        res = new ResultResponse(Constants.STATUS_OK, Constants.MESSAGE_OK, id);
        return res;
    }

    @ApiOperation("修改一条用户数据")
    @PutMapping
    public ResultResponse updateUser(@RequestBody User user){
        ResultResponse res = null;
        int i = userMapper.updateById(user);
        res = new ResultResponse(Constants.STATUS_OK, Constants.MESSAGE_OK, user);
        return res;
    }


    @ApiOperation("查询所有用户数据")
    @GetMapping
    public ResultResponse queryEmployee(@RequestParam("page") int pageNo, @RequestParam("limit") int limit, @RequestParam("sort") String idSort){
        ResultResponse res = null;
        List<User > users = userMapper.selectList(null);
        log.info("users====>"+users);
        MyPage page = this.userService.searchUser(pageNo, limit, idSort,users);
        res = new ResultResponse(Constants.STATUS_OK, Constants.MESSAGE_OK, page);
        return res;
    }

}

