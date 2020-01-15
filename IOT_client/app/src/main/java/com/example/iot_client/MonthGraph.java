package com.example.iot_client;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.data.BarData;
import java.util.ArrayList;

public class MonthGraph extends AppCompatActivity {
    BarChart oilChart;
    LineChart fuelChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_graph);
        oilChart = (BarChart) findViewById(R.id.oilChart);
        fuelChart = (LineChart) findViewById(R.id.fuelChart);

        //Line차트 init
        ArrayList<Entry> lineEntry = new ArrayList<>();
        lineEntry.add(new Entry(11.65f,0));
        lineEntry.add(new Entry(10.18f,1));
        lineEntry.add(new Entry(11.19f,2));
        lineEntry.add(new Entry(11.24f,4));
        lineEntry.add(new Entry(12.49f,5));
        lineEntry.add(new Entry(12.22f,6));
        lineEntry.add(new Entry(12.43f,7));
        lineEntry.add(new Entry(12.93f,8));
        lineEntry.add(new Entry(12.33f,9));
        lineEntry.add(new Entry(15.19f,10));
        lineEntry.add(new Entry(11.53f,11));
        lineEntry.add(new Entry(10.9f,12));


        LineDataSet lineDataSet = new LineDataSet(lineEntry,"주유비");
        ArrayList<String> lineLabels = new ArrayList<>();
        lineLabels.add("1월");
        lineLabels.add("2월");
        lineLabels.add("3월");
        lineLabels.add("4월");
        lineLabels.add("5월");
        lineLabels.add("6월");
        lineLabels.add("7월");
        lineLabels.add("8월");
        lineLabels.add("9월");
        lineLabels.add("10월");
        lineLabels.add("11월");
        lineLabels.add("12월");

        XAxis xAxis =fuelChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//차트의 아래 xAxis
        xAxis.setTextSize(10f); //xAxis에 표출되는 텍스트의 크기는 10f
        xAxis.setDrawGridLines(false);//그리드 라인 없애기

        //차트의 왼쪽 xAxis
        YAxis leftAxis = fuelChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);

        //차트 오른쪽 xAxis
        YAxis rightAxis = fuelChart.getAxisRight();
        rightAxis.setEnabled(false);//right는 비활성화
        LineData data = new LineData(lineLabels,lineDataSet);
        fuelChart.setData(data);

        //BarChart init
        ArrayList<BarEntry> entries =  new ArrayList<>();
        entries.add(new BarEntry(16f, 0));
        entries.add(new BarEntry(18f, 1));
        entries.add(new BarEntry(25f, 2));
        entries.add(new BarEntry(16f, 3));
        entries.add(new BarEntry(13f, 4));
        entries.add(new BarEntry(16f, 5));
        entries.add(new BarEntry(35f, 6));
        BarDataSet dataset = new BarDataSet(entries, "\n" + "Fuel price ");
        //x축 레이블 정의
        ArrayList<String>labels = new ArrayList<>();
        labels.add("8월");
        labels.add("9월");
        labels.add("10월");
        labels.add("11월");
        labels.add("12월");
        labels.add("1월");
        labels.add("2월");
        //데이터 넣기
        BarData  oildata = new BarData(labels,dataset);
        oilChart.setData(oildata);
        dataset.setColors(ColorTemplate.JOYFUL_COLORS);
        dataset.setValueTextColor(Color.GRAY);
        dataset.setValueTextSize(18f);
    }
    public LineDataSet createSet(){
        LineDataSet set = new LineDataSet(null,"\n" + "Fuel efficiency");//데이터 셋의 이름을 "Fuel efficiency"로 설정
        set.setAxisDependency(YAxis.AxisDependency.LEFT);// Axis를 YAxis의 LEFT를 기본으로 설정
        set.setColor(ColorTemplate.getHoloBlue()); //데이터 라인 색을 HoloBlue로 설정
        set.setCircleColor(Color.WHITE); //데이터 점을 WHITE로 설정
        set.setLineWidth(2f);//라인의 두께는 2f
        set.setCircleRadius(4f);//데이터 점의 반지름을 4f로
        set.setFillAlpha(64);//투명도를 65
        set.setFillColor(ColorTemplate.rgb("#ffc000"));//채우기 색을 yellow
        set.setHighLightColor(Color.rgb(244,117,117));//선택시 색 설정
        set.setDrawValues(true);//각 데이터 값을 텍스트로 나타내게 true
        return set;

    }
}
