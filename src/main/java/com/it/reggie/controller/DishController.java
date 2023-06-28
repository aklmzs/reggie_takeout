package com.it.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.reggie.common.R;
import com.it.reggie.dto.DishDto;
import com.it.reggie.entity.Category;
import com.it.reggie.entity.Dish;
import com.it.reggie.entity.DishFlavor;
import com.it.reggie.entity.Employee;
import com.it.reggie.service.CategoryService;
import com.it.reggie.service.DishFlavorService;
import com.it.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//菜品管理
@RestController
@RequestMapping("/dish")
@Slf4j
@Transactional
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;

    //新增菜品
    @PostMapping
    @CacheEvict(value = "dishCache",allEntries = true)
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);
        //String key= "dish_" + dishDto.getCategoryId()+"_1";
        //redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }
    //分页查询
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        //分页构造器
        Page<Dish> pageInfo = new Page(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper();
        //过滤条件
        queryWrapper.like(StringUtils.hasText(name),Dish::getName,name);
        //排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list=records.stream().map((item)->{
            DishDto dishDto = new DishDto();
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            BeanUtils.copyProperties(item,dishDto);
            return dishDto;
        }).collect(Collectors.toList()); ;

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }
    //修改菜品
    @PutMapping
    @CacheEvict(value = "dishCache",allEntries = true)
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        // Set keys = redisTemplate.keys("dish_*");
        //String key= "dish_" + dishDto.getCategoryId()+"_1";
        //redisTemplate.delete(key);
        return R.success("菜品修改成功");
    }

    //修改菜品状态
    @PostMapping("/status/{pathVariable}")
    @CacheEvict(value = "dishCache",allEntries = true)
    public R<String> updateStatus(@PathVariable("pathVariable") int status, @RequestParam("ids") String[] ids) {
        log.info("要修改的菜品id: {}", ids);
        log.info("路径变量: {}", status);
        for(String id:ids){
            Dish dish = dishService.getById(id);
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        /*LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Dish::getId, ids);
        List<Dish> dishes = dishService.list(lambdaQueryWrapper);
        for (Dish dish : dishes) {
            String key = "dish_" + dish.getCategoryId() + "_1";
            redisTemplate.delete(key);
        }*/
        return R.success("菜品状态修改成功");
    }
    @DeleteMapping
    public R<String> delete(Long[] ids){
        dishService.deleteWithFlavor(ids);
        return R.success("菜品删除成功");
    }
    //根据条件查菜品数据
    @GetMapping("/list")
    @Cacheable(value ="dishCache",key = "#dish.categoryId+'_'+#dish.status")
    public R<List<DishDto>> list(Dish dish){
        //从redis中获取缓存数据
        /*String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        List<DishDto> dishDtoList= (List<DishDto>) redisTemplate.opsForValue().get(key);
        if(dishDtoList!=null){
            return R.success(dishDtoList);
        }
*/
        //查询条件
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        //排序
        queryWrapper.orderByAsc((Dish::getSort)).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        List<DishDto> dishDtoList=list.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long dishid = item.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId,dishid);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;

        }).collect(Collectors.toList());
        //redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);
        return  R.success(dishDtoList);
    }
}


