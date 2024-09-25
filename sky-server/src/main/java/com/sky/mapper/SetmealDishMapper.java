package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    /**
     * 根据id删除套餐菜品数据
     * @param id
     */
    @Delete("delete from setmeal_dish where id = #{id}")
    void delete(Long id);

    /**
     * 根据套餐id查询套餐和菜品的关联关系
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getSetmealId(Long id);
}
