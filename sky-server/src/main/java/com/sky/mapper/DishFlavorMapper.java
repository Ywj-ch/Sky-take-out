package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 向菜品口味关系表插入一条或者多条数据
     *
     * @param dishFlavorList
     */
    void insert(List<DishFlavor> dishFlavorList);

    @Delete("delete from dish_flavor where id = #{dishId}")
    void deleteByDishId(Long id);
}
