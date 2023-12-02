package com.example.journalapp.utils;

import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;

import com.example.journalapp.database.entity.Folder;
import com.example.journalapp.database.entity.Note;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GraphHelperUtil {

    public static void setupBarChart(BarChart barchart, List<Note> allNotes) {
        barchart.getAxisRight().setDrawLabels(false);

        ArrayList<BarEntry> entries = new ArrayList<>();

        String prevDate = "";
        List<Note> currentNoteList = new ArrayList<>();
        List<List<Note>> noteListsGroupedByDate = new ArrayList<>();

        for (Note note : allNotes) {
            String currentDate = ConversionUtil.trimDateToDay(note.getCreatedDate());

            if (!prevDate.equals(currentDate)) {
                if (!currentNoteList.isEmpty()) {

                    noteListsGroupedByDate.add(currentNoteList);
                }

                currentNoteList = new ArrayList<>();
            }

            currentNoteList.add(note);
            prevDate = currentDate;
        }


        if (!currentNoteList.isEmpty()) {
            noteListsGroupedByDate.add(currentNoteList);
        }

        int xIndex = 0;
        for (List<Note> noteList : noteListsGroupedByDate) {
            entries.add(new BarEntry(xIndex, findAverageOfEachEmotion(noteList)));
            xIndex++;
        }

        // check dark mode
        int currentNightMode = barchart.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;
        // Set colors based on the mode
        int textColor = isDarkMode ? Color.WHITE : Color.BLACK;

        YAxis yAxis = barchart.getAxisLeft();
        yAxis.setTextSize(10f);
        yAxis.setAxisMinimum(1f);
        yAxis.setAxisMaximum(5f);
        yAxis.setLabelCount(5, true);
        List<String> yValues = Arrays.asList("","😡", "😠", "😐", "😊", "😄");
        yAxis.setValueFormatter(new IndexAxisValueFormatter(yValues));

        BarDataSet dataset = new BarDataSet(entries, "");
        dataset.setDrawValues(false);
        dataset.setColors(ColorTemplate.MATERIAL_COLORS);
        dataset.setValueTextSize(14f);
        BarData barData = new BarData(dataset);
        barchart.setData(barData);

        barchart.getDescription().setEnabled(false);
        barchart.invalidate();

        XAxis xAxis = barchart.getXAxis();
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(30f);
        xAxis.setTextColor(textColor);
        barchart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barchart.getXAxis().setGranularity(1f);
        barchart.getXAxis().setGranularityEnabled(true);


    }
    public static float findAverageOfEachEmotion(List<Note> allNotes) {
        if (allNotes.isEmpty()) {
            return 0f;
        }

        int emotionalSum = 0; // 5
        int count = 0; // 1

        for (Note note : allNotes) {
            emotionalSum = emotionalSum + note.getEmotion();
            count++;
        }

        return ((float) emotionalSum / count) ;
    }



}

