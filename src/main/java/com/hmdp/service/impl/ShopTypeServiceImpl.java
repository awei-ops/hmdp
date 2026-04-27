package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        // 1. 从Redis查询JSON字符串列表
        List<String> shopTypeJsonList = stringRedisTemplate.opsForList().range("shop:type:list", 0, -1);

        // 2. Redis有数据，转成ShopType对象列表返回
        if (shopTypeJsonList != null && !shopTypeJsonList.isEmpty()) {
            List<ShopType> shopTypes = new ArrayList<>();
            for (String json : shopTypeJsonList) {
                ShopType type = JSONUtil.toBean(json, ShopType.class);
                shopTypes.add(type);
            }
            return Result.ok(shopTypes);
        }

        // 3. Redis没数据，查数据库
        List<ShopType> sortList = this.query().orderByAsc("sort").list();

        // 4. 存入Redis（这次存标准JSON）
        for (ShopType type : sortList) {
            stringRedisTemplate.opsForList().rightPush("shop:type:list", JSONUtil.toJsonStr(type));
        }

        return Result.ok(sortList);
    }
}
