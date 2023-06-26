package com.it.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.reggie.dto.DishDto;
import com.it.reggie.entity.Dish;
import org.springframework.stereotype.Service;
@Service
public interface DishService extends IService<Dish> {
    public void saveWithFlavor(DishDto dishDto);
    public DishDto getByIdWithFlavor(Long id);


    public void updateWithFlavor(DishDto dishDto);
    public void deleteWithFlavor(Long[] ids);
}
