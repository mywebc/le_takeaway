package com.chenxiaolani.le_takeaway.dto;

import com.chenxiaolani.le_takeaway.entity.Setmeal;
import com.chenxiaolani.le_takeaway.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
