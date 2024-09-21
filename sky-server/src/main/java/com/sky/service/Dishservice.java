package com.sky.service;

import com.sky.dto.DishDTO;
import org.springframework.stereotype.Service;

@Service
public interface Dishservice {
    /**
     * 新增菜品
     * @param dishDTO
     */
    void addDish(DishDTO dishDTO);
}
