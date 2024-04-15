package com.example.myfoodchoice.AdapterRecyclerView;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfoodchoice.AdapterInterfaceListener.OnActionDetailMealListener;
import com.example.myfoodchoice.AdapterInterfaceListener.OnActionMealListener;
import com.example.myfoodchoice.ModelCaloriesNinja.FoodItem;
import com.example.myfoodchoice.ModelMeal.Meal;
import com.example.myfoodchoice.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealHistoryAdapter extends RecyclerView.Adapter<MealHistoryAdapter.myViewHolder>
{
    private ArrayList<Meal> mealArrayList;

    private final OnActionMealListener onActionMealListener;

    public MealHistoryAdapter(ArrayList<Meal> mealArrayList,
                              OnActionMealListener onActionMealListener
    )
    {
        this.mealArrayList = mealArrayList;
        this.onActionMealListener = onActionMealListener;
    }

    public void updateMeals(@NonNull ArrayList<Meal> newMeals)
    {
        int oldSize = this.mealArrayList.size();
        int newSize = newMeals.size();

        this.mealArrayList = newMeals;

        // Notify the adapter of the changes
        if (oldSize == 0)
        {
            notifyDataSetChanged(); // this is not efficient
        }
        else
        // this is more efficient way
        {
            // If the old list was not empty, notify the adapter of the specific changes
            if (newSize > oldSize)
            {
                // If the new list is larger, notify the adapter of the added items
                notifyItemRangeInserted(oldSize, newSize - oldSize);
            }
            else if
            (newSize < oldSize)
            {
                // If the new list is smaller, notify the adapter of the removed items
                notifyItemRangeRemoved(newSize, oldSize - newSize);
            }
            else
            {
                // If the list sizes are the same, notify the adapter of the changed items
                notifyItemRangeChanged(0, newSize);
            }
        }
    }

    public static class myViewHolder extends RecyclerView.ViewHolder
    {
        // todo: init here for more item, I am not sure which attribute should be included.
        public TextView timeText;

        public TextView dateText;

        public TextView mealNumText;

        public RecyclerView foodDetailRecyclerView;

        public myViewHolder(final View itemView, OnActionMealListener onActionMealListener)
        {
            super(itemView);

            // todo: we might need to change this if we don't use the view button
            timeText = itemView.findViewById(R.id.timeMealText);
            dateText = itemView.findViewById(R.id.dateMealText);
            mealNumText = itemView.findViewById(R.id.mealNumText);
            foodDetailRecyclerView = itemView.findViewById(R.id.foodDetailsRecyclerView);
            foodDetailRecyclerView.setVisibility(View.GONE);

            itemView.setOnClickListener(v ->
            {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION)
                {
                    onActionMealListener.onClickMeal(position);
                    // Toggle the visibility of the inner RecyclerView

                    if (foodDetailRecyclerView.getVisibility() == View.GONE)
                    {
                        foodDetailRecyclerView.setVisibility(View.VISIBLE);
                        // Optionally, load or update the data for the inner RecyclerView here
                    }
                    else
                    {
                        foodDetailRecyclerView.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
    @NonNull
    @Override
    public MealHistoryAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.meal_history_item_layout,
                parent, false);
        return new MealHistoryAdapter.myViewHolder(itemView, onActionMealListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MealHistoryAdapter.myViewHolder holder, int position)
    {
        Meal meal = mealArrayList.get(position);

        // format time based on Locale.English
        SimpleDateFormat sdfDate = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH);
        SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

        Date date = meal.getDate();

        // format both date and time.
        String formattedDate = sdfDate.format(date);
        String formattedTime = sdfTime.format(date);

        // fixme: the problem is that in all meal should display item num correctly?
        if (meal.isMorning())
        {
            holder.mealNumText.setText(String.format(Locale.ROOT, "Breakfast %d", position + 1));
        }

        if (meal.isAfternoon())
        {
            holder.mealNumText.setText(String.format(Locale.ROOT, "Lunch %d", position + 1));
        }

        if (meal.isNight())
        {
            holder.mealNumText.setText(String.format(Locale.ROOT, "Dinner %d", position + 1));

        }

        // Set up the inner RecyclerView
        holder.foodDetailRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        List<FoodItem.Item> items = meal.getDishes().getItems();
        MealDetailHistoryAdapter innerAdapter = new MealDetailHistoryAdapter(items); // Assuming Meal has a getDishes() method that returns a list of dishes
        holder.foodDetailRecyclerView.setAdapter(innerAdapter);

        holder.dateText.setText(formattedDate);
        holder.timeText.setText(formattedTime);
    }

    @Override
    public int getItemCount()
    {
        return mealArrayList.size();
    }
}