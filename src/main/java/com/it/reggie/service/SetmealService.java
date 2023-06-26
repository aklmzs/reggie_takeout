package com.it.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.it.reggie.dto.SetmealDto;
import com.it.reggie.entity.Setmeal;
import org.springframework.stereotype.Service;

@Service
public interface SetmealService extends IService<Setmeal> {
    public void saveWithDish(SetmealDto setmealDto);

    public void updateWithDish(SetmealDto setmealDto);

    public SetmealDto getByIdWithDish(Long id);

    public void deleteWithDish(Long[] ids);
}
