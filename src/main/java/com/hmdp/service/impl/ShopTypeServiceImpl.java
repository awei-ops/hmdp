package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public Result getList() {
        List<String> shoptypelist = stringRedisTemplate.opsForList().range("shop:cash:listKey", 0, -1);
        if (!shoptypelist.isEmpty()) {
            return Result.ok(shoptypelist);
        }

        return null;
    }
//    String key = "cash:shop:" + id;
//
//    // 1. 从Redis Hash查询
//    Map<Object, Object> shopMap = stringRedisTemplate.opsForHash().entries(key);
//
//        if (!shopMap.isEmpty()) {
//        // 把Map转成Shop对象
//        Shop shop = BeanUtil.fillBeanWithMap(shopMap, new Shop(), false);
//        return Result.ok(shop);
//    }
//
//    // 2. 查询数据库
//    Shop shop = getById(id);
//        if (shop == null) {
//        return Result.fail("数据不存在");
//    }
//
//    // 3. 关键：把对象转成 Map<String, String>
//    Map<String, Object> map = BeanUtil.beanToMap(shop, new HashMap<>(),
//            CopyOptions.create()
//                    .setIgnoreNullValue(true)
//                    .setFieldValueEditor((fieldName, fieldValue) ->
//                            fieldValue == null ? null : fieldValue.toString())
//    );
//
//    // 4. 存入Hash
//        stringRedisTemplate.opsForHash().putAll(key, map);
//
//        return Result.ok(shop);

}
