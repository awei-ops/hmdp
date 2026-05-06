package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.BooleanUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        String key = "cash:shop:" + id;

        // 1. 从Redis Hash查询
        Map<Object, Object> shopMap = stringRedisTemplate.opsForHash().entries(key);

        // 2. 判断缓存是否存在
        if (!shopMap.isEmpty()) {
            // 这里判断是否是 缓存的空值标记（你可以用一个特殊hash标记空）
            if(shopMap.containsKey("empty")){
                // 空值缓存，直接返回不存在
                return Result.fail("数据不存在");
            }
            // 正常数据，转为对象
            Shop shop = BeanUtil.fillBeanWithMap(shopMap, new Shop(), false);
            return Result.ok(shop);
        }

        // 3. 缓存不存在，查询数据库
        Shop shop = getById(id);
        if (shop == null) {
            // 缓存空值（解决缓存击穿）
            Map<String, Object> emptyMap = new HashMap<>();
            emptyMap.put("empty", "true"); // 用一个标记表示空
            stringRedisTemplate.opsForHash().putAll(key, emptyMap);
            stringRedisTemplate.expire(key, 1, TimeUnit.MINUTES); // 短过期时间
            return Result.fail("数据不存在");
        }

        // 4. 查询到，存入Redis Hash
        Map<String, Object> map = BeanUtil.beanToMap(shop, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) ->
                                fieldValue == null ? null : fieldValue.toString())
        );

        stringRedisTemplate.opsForHash().putAll(key, map);
        stringRedisTemplate.expire(key, 30, TimeUnit.MINUTES);
        return Result.ok(shop);
    }
    private boolean trylock(String key){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }
    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }
    @Override
    public Result updateRedis(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("商品id为空");
        }
        updateById(shop);
        stringRedisTemplate.delete("cash:shop:"+id);
        return Result.ok();
    }
}
