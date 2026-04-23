package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryById(Long id) {
        //从redis里查询
        String s = stringRedisTemplate.opsForValue().get("cash:shop:"+id);
        //判断是否cunzai
        if (StrUtil.isNotBlank(s)) {
            //存在，直接返回
            return Result.ok(s);
        }
        //不存在，根据id查询数据库
        Shop shop = getById(id);
        if(shop==null){
            return Result.fail("数据不存在");
        }
        stringRedisTemplate.opsForValue().set("cash:shop:"+id, JSONUtil.toJsonStr(shop));
        return Result.ok(shop);
    }
}
