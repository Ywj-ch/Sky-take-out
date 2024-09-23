package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper{
    /**
     * 根据菜品id来查找关联的套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 根据套餐id来新增菜品id
     * @param setmealDishes
     */
    void insertBath(List<SetmealDish> setmealDishes);
}
