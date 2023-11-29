package com.example.journalapp.ui.main;

import android.graphics.Color;

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

public class GraphHelper {

    public static void setupBarChart(BarChart barchart, List<Note> allNotes) {
            barchart.getAxisRight().setDrawLabels(false);

            ArrayList<BarEntry> entries = new ArrayList<>();

            entries.add(new BarEntry(0, findAverageOfEachEmotion(allNotes, 1)));
            entries.add(new BarEntry(1, findAverageOfEachEmotion(allNotes, 2)));
            entries.add(new BarEntry(2, findAverageOfEachEmotion(allNotes, 3)));
            entries.add(new BarEntry(3, findAverageOfEachEmotion(allNotes, 4)));
            entries.add(new BarEntry(4, findAverageOfEachEmotion(allNotes, 5)));


            YAxis yAxis = barchart.getAxisLeft();
            yAxis.setTextSize(12f);
            yAxis.setAxisMaximum(100f);
            yAxis.setAxisLineWidth(2f);
            yAxis.setAxisLineColor(Color.BLACK);
            yAxis.setLabelCount(10);

            BarDataSet dataset = new BarDataSet(entries, "Emojis");
            dataset.setColors(ColorTemplate.MATERIAL_COLORS);
            dataset.setValueTextSize(14f);
            BarData barData = new BarData(dataset);
            barchart.setData(barData);

            barchart.getDescription().setEnabled(false);
            barchart.invalidate();

            List<String> xValues = Arrays.asList("😡", "😠", "😐", "😊", "😄");
            barchart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xValues));
            barchart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            barchart.getXAxis().setGranularity(1f);
            barchart.getXAxis().setGranularityEnabled(true);


    }
    private static float findAverageOfEachEmotion(List<Note> allNotes, int emotionValue) {
        if (allNotes.isEmpty()) {
            return 0f;
        }

        int totalNotes = allNotes.size();
        int count = 0;

        for (Note note : allNotes) {
            if (note.getEmotion() == emotionValue) {
                count++;
            }
        }

        return ((float) count / totalNotes) * 100;
    }

}
