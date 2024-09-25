package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐,同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //向菜品表插入一条数据
        setmealMapper.insert(setmeal);
        //获取insert语句的id值
        //获取生成的套餐id通过Mapper层的：useGeneratedKeys="true" keyProperty="id"获取插入后生成的主键值
        //套餐菜品关系表的setmealId页面不能传递，它是向套餐表插入数据之后生成的主键值，也就是套餐菜品关系表的逻辑外键setmealId
        Long setmealId = setmeal.getId();
        //套餐表更新后，套餐菜品表也要更新
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            //保存套餐和菜品的关联关系  动态sql批量插入
            setmealDishMapper.insertBath(setmealDishes);
        }
    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageSetmeal(SetmealPageQueryDTO setmealPageQueryDTO) {
        //开始分页查询
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        long total = page.getTotal();
        List<SetmealVO> records = page.getResult();
        return new PageResult(total, records);
    }

    /**
     * 批量删除套餐
     */
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断当前套餐是否能够删除---是否存在起售中的套餐？？
        //思路：遍历获取传入的id，根据id查询套餐setmeal中的status字段，0 停售 1 起售，
        //如果是1代表是起售状态不能删除
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (StatusConstant.ENABLE.equals(setmeal.getStatus())) {
                //起售中的套餐不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
            //删除setmeal表的数据
            setmealMapper.delete(id);
            //删除setmeal_dish表的数据
            setmealDishMapper.delete(id);
        }
    }

    /**
     * 根据id查询套餐
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getSetmealByid(Long id) {
        //先将套餐数据返回
        Setmeal setmeal = setmealMapper.getById(id);
        //再将套餐菜品数据返回
        List<SetmealDish> setmealDishes = setmealDishMapper.getSetmealId(id);
        //封装返回结果
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     *修改套餐
     */
    @Transactional
    @Override
    public void update(SetmealDTO setmealDTO) {
        //先将前端传来的数据拷贝到实体层
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //1.修改套餐的基本信息
        setmealMapper.update(setmeal);
        //2.根据id删除套餐和菜品的关联关系，操作setmeal_dish表，执行delete
        setmealDishMapper.delete(setmealDTO.getId());
        //获取页面传来的套餐和菜品关系表数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //获取套餐表的id
        Long setmealId = setmealDTO.getId();
        //和之前修改菜品不同，菜品的口味不是必须的所以用if，而这里菜品是必须的，不存在不含菜品的套餐
        for(SetmealDish setmealDish : setmealDishes){
            setmealDish.setSetmealId(setmealId);
        }
        //3.重新插入套餐和菜品的关联关系，操作setmeal_dish表，执行insert
        setmealDishMapper.insertBath(setmealDishes);

    }

    /**
     *套餐起售停售
     */
    @Override
    public void status(Integer status, Long id) {
        //停售套餐时不需要判断菜品是否停售
        //起售套餐时，就必须判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if(StatusConstant.ENABLE.equals(status)){//判断是否为停售状态
            List<Dish> dishes = dishMapper.getBySetmealId(id);//根据套餐id获取到菜品数据
            if(dishes != null && !dishes.isEmpty()){//判断菜品是否为空
                for(Dish dish : dishes){
                    if(StatusConstant.DISABLE.equals(dish.getStatus())){//判断是否有停售状态都菜品
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                }
            }
        }
        //菜品停售
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status).
                build();
        setmealMapper.update(setmeal);
    }


}









