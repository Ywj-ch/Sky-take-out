package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 向菜品口味关系表插入一条或者多条数据
     *
     * @param dishFlavorList
     */
    void insert(List<DishFlavor> dishFlavorList);

    /**
     * 根据菜品id批量删除口味
     * @param DishIds
     */
    void deleteByDishIds(List<Long> DishIds);

    /**
     * 根据dishId查询口味数据
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dishId} ")
    List<DishFlavor> getByDishId(Long dishId);



}
