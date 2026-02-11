package com.synergy.model;

import java.util.ArrayList;
import java.util.List;

public class TaskGroup extends Activity {
    private static final long serialVersionUID = 1L;

    // La lista dei figli (che possono essere Task o altri Gruppi)
    private List<Activity> children = new ArrayList<>();

    public TaskGroup(int id, String title, PriorityLevel priority) {
        super(id, title, priority);
    }

    public void addActivity(Activity a) {
        children.add(a);
    }
    
    public void removeActivity(Activity a) {
        children.remove(a);
    }

    public List<Activity> getChildren() {
        return children;
    }
}