package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            Result.fail("输入的手机号格式有误");
        }
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set("login:code"+phone,code,2, TimeUnit.MINUTES);
        log.debug("发送验证码成功:"+code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone=loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(loginForm.getPhone())) {
            Result.fail("输入的手机号格式有误");
        }
        String cashcode = stringRedisTemplate.opsForValue().get("login:code" + phone);
        String code = loginForm.getCode();
        if (cashcode==null||!cashcode.equals(code)) {
            Result.fail("输入的验证码有误");
        }
        User user = query().eq("phone", phone).one();
        if(user==null){
            user=createUserByPhone(phone);
        }
        String loginUserKey = RedisConstants.LOGIN_USER_KEY;
        String token = UUID.randomUUID().toString();
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(userDTO,new HashMap<>()
        , CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName,fieldValue)->fieldValue.toString()));
        stringRedisTemplate.opsForHash().putAll(loginUserKey+token,stringObjectMap);
        //设置有效期
        stringRedisTemplate.expire(loginUserKey+token,30,TimeUnit.MINUTES);
        return Result.ok(token);
    }

    private User createUserByPhone(String phone) {
        User user=new User();
        user.setPhone(phone);
        user.setNickName("User-"+ RandomUtil.randomString(6));
        save(user);
        return user;
    }
}
