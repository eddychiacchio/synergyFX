package com.synergy.factory;

import com.synergy.model.Activity;
import com.synergy.model.PriorityLevel;
import java.time.LocalDate;

public interface IActivityFactory {
    Activity createActivity(String title, PriorityLevel priority, LocalDate deadline, String[] subTasks);
}