package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.common.CustomException;
import com.it.reggie.entity.Category;
import com.it.reggie.entity.Dish;
import com.it.reggie.entity.Setmeal;
import com.it.reggie.mapper.CategoryMapper;
import com.it.reggie.service.CategoryService;
import com.it.reggie.service.DishService;
import com.it.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {



    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    //根据id删除分类，分类前需要判断
    @Override
    public void remove(Long id) {

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件根据id
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);

        //查询当前分类是否关联菜品
        int count1 = dishService.count(dishLambdaQueryWrapper);
        if (count1>0){
            throw new CustomException("当前分类已关联菜品，不能删除");
        }
        //查询当前分类是否关联套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper =new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2= setmealService.count(setmealLambdaQueryWrapper);
        if (count2>0){
            throw new CustomException("当前分类已关联套餐，不能删除");
        }

        //正常删除
        super.removeById(id);

    }
}
